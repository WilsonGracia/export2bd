import { Module } from '@nestjs/common';
import { ExportController } from './export.controller';
import { ExportService } from './export.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Control } from './entities/control.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Control])],
  controllers: [ExportController],
  providers: [ExportService],
})
export class ExportModule {}
