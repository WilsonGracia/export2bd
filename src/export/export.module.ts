import { Module } from '@nestjs/common';
import { ExportController } from './export.controller';
import { ExportService } from './export.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Control } from './entities/control.entity';
import { fileRepository } from './repository/fileRepository';

@Module({
  imports: [TypeOrmModule.forFeature([Control])],
  controllers: [ExportController],
  providers: [ExportService, fileRepository],
})
export class ExportModule {}
