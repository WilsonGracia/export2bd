import { Module } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { AuthService } from './auth.service';
import { AuthController } from './auth.controller';
import { JwtAuthGuard } from './guards/jwt-auth.guard';
import { DynamicDatabaseModule } from '../dynamic-database/dynamic-database.module';

@Module({
  imports: [
    DynamicDatabaseModule,
    JwtModule.register({
      secret: process.env.JWT_SECRET || 'justTesting', //! change this in production
      signOptions: {
        expiresIn: process.env.JWT_EXPIRATION
          ? Number(process.env.JWT_EXPIRATION)
          : 3600,
        algorithm: 'HS256',
      },
    }),
  ],
  controllers: [AuthController],
  providers: [AuthService, JwtAuthGuard],
  exports: [AuthService, JwtAuthGuard],
})
export class AuthModule {}
