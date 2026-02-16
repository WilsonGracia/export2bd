import { Module } from '@nestjs/common';
import { ExportController } from './export.controller';
import { ExportService } from './export.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Control } from './entities/control.entity';
import { fileRepository } from './repository/fileRepository';
import { DynamicDatabaseModule } from 'src/dynamic-database/dynamic-database.module';
import { AuthModule } from 'src/auth/auth.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([Control]),
    DynamicDatabaseModule,
    AuthModule,
  ],
  controllers: [ExportController],
  providers: [ExportService, fileRepository],
})
export class ExportModule {}
