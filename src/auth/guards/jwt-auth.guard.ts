import {
  Injectable,
  CanActivate,
  ExecutionContext,
  UnauthorizedException,
  createParamDecorator,
} from '@nestjs/common';
import { AuthService } from '../auth.service';
import { DatabaseCredentialsDto } from '../../export/dto/inputs/DatabaseCredentialsDto';

@Injectable()
export class JwtAuthGuard implements CanActivate {
  constructor(private readonly authService: AuthService) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const request = context.switchToHttp().getRequest();
    const authHeader = request.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      throw new UnauthorizedException('No token provided');
    }

    const token = authHeader.substring(7);

    try {
      // Validar el token y extraer las credenciales
      const credentials = await this.authService.validateToken(token);

      // Inyectar las credenciales en el request
      request.dbCredentials = credentials;

      return true;
    } catch (error) {
      throw new UnauthorizedException('Invalid or expired token');
    }
  }
}

/**
 * Decorator para extraer las credenciales del request
 */
export const DbCredentials = createParamDecorator(
  (data: unknown, ctx: ExecutionContext): DatabaseCredentialsDto => {
    const request = ctx.switchToHttp().getRequest();
    return request.dbCredentials;
  },
);
