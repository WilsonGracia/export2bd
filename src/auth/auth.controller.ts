import {
  Body,
  Controller,
  Post,
  Headers,
  UnauthorizedException,
} from '@nestjs/common';
import { AuthService } from './auth.service';
import { DatabaseCredentialsDto } from '../export/dto/inputs/DatabaseCredentialsDto';

@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('login')
  async login(@Body() credentials: DatabaseCredentialsDto) {
    return this.authService.login(credentials);
  }

  //! refresh token endpoint could be added here in the future
  @Post('refresh')
  async refresh(@Headers('authorization') authHeader: string) {
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      throw new UnauthorizedException('No token provided');
    }

    const token = authHeader.substring(7);
    return this.authService.refreshToken(token);
  }

  //! to validate token and see the credentials (for testing purposes)
  @Post('validate')
  async validate(@Headers('authorization') authHeader: string) {
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      throw new UnauthorizedException('No token provided');
    }

    const token = authHeader.substring(7);
    const credentials = await this.authService.validateToken(token);

    return {
      valid: true,
      user: {
        database: credentials.database,
        host: credentials.host,
        username: credentials.username,
      },
    };
  }
}
