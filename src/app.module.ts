import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { typeOrmConfig } from './database/typeorm.config';
import { DatabaseModule } from './database/database.module';
import { ExportModule } from './export/export.module';
import { DynamicDatabaseModule } from './dynamic-database/dynamic-database.module';
import { AuthModule } from './auth/auth.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    TypeOrmModule.forRootAsync({
      inject: [ConfigService],
      useFactory: (config: ConfigService) => typeOrmConfig(config),
    }),
    DatabaseModule,
    ExportModule,
    DynamicDatabaseModule,
    AuthModule,
  ],
})
export class AppModule {}
