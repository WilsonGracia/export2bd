import { TypeOrmModuleOptions } from '@nestjs/typeorm';
import { ConfigService } from '@nestjs/config';

export const typeOrmConfig = (config: ConfigService): TypeOrmModuleOptions => ({
  type: 'postgres',
  host: config.get<string>('DB_HOST') || 'localhost',
  port: Number(config.get<string>('DB_PORT')) || 5432,
  username: config.get<string>('DB_USER') || 'postgres',
  password: config.get<string>('DB_PASS') || 'postgres',
  database: config.get<string>('DB_NAME') || 'postgres',
  autoLoadEntities: true,
  synchronize: false,
  logging: false,
  retryAttempts: 0,
});
