import {
  Injectable,
  UnauthorizedException,
  BadRequestException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { DatabaseCredentialsDto } from '../export/dto/inputs/DatabaseCredentialsDto';
import { DynamicDatabaseService } from '../dynamic-database/dynamic-database.service';
import * as crypto from 'crypto';

export interface TokenPayload {
  sub: string;
  credentials: DatabaseCredentialsDto;
  iat?: number;
  exp?: number;
}

@Injectable()
export class AuthService {
  constructor(
    private readonly jwtService: JwtService,
    private readonly dynamicDbService: DynamicDatabaseService,
  ) {}

  async login(credentials: DatabaseCredentialsDto): Promise<{
    access_token: string;
    expires_in: number;
  }> {
    if (!credentials) {
      throw new BadRequestException('credentials are required');
    }

    const isValid =
      await this.dynamicDbService.validateCredentials(credentials);

    if (!isValid) {
      throw new UnauthorizedException('Invalid database credentials');
    }

    const userId = this.generateUserId(credentials);

    const payload: TokenPayload = {
      sub: userId,
      credentials: credentials,
    };

    const expiresIn = 3600; //!hora
    const access_token = this.jwtService.sign(payload, {
      expiresIn,
    });

    //! console.log('si entra');

    return {
      access_token,
      expires_in: expiresIn,
    };
  }

  //! valida el token y extrae las credenciales
  async validateToken(token: string): Promise<DatabaseCredentialsDto> {
    try {
      const payload = this.jwtService.verify<TokenPayload>(token);

      if (!payload.credentials) {
        throw new UnauthorizedException(
          'Invalid token payload: missing credentials',
        );
      }

      return payload.credentials;
    } catch (error) {
      if (error instanceof UnauthorizedException) {
        throw error;
      }
      throw new UnauthorizedException('Invalid or expired token');
    }
  }

  async refreshToken(oldToken: string): Promise<{
    access_token: string;
    expires_in: number;
  }> {
    const credentials = await this.validateToken(oldToken);
    return this.login(credentials);
  }

  private generateUserId(credentials: DatabaseCredentialsDto): string {
    const data = `${credentials.username}@${credentials.host}:${credentials.port}/${credentials.database}`;
    return crypto.createHash('sha256').update(data).digest('hex');
  }
}
