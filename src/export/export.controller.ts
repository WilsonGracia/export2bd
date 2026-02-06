import {
  Body,
  Controller,
  Post,
  UploadedFile,
  UseInterceptors,
} from '@nestjs/common';
import { ExportService } from './export.service';
import { CreateControlDto } from './dto/inputs/CreateControlDto';
import { ControlResponseDto } from './dto/outputs/ControlResponseDto';
import { FileInterceptor } from '@nestjs/platform-express';
import * as XLSX from 'xlsx';

@Controller('export')
export class ExportController {
  constructor(private readonly exportService: ExportService) {}

  @Post('control')
  async createControl(
    @Body() dto: CreateControlDto,
  ): Promise<ControlResponseDto> {
    return this.exportService.createControl(dto);
  }

  @Post('upload')
  @UseInterceptors(FileInterceptor('file'))
  async uploadFile(@UploadedFile() file: Express.Multer.File) {
    const workbook = XLSX.read(file.buffer, { type: 'buffer' });

    const sheetName = workbook.SheetNames[0];
    const worksheet = workbook.Sheets[sheetName];

    const rows: Record<string, any>[] = XLSX.utils.sheet_to_json(worksheet);
    return await this.exportService.insertControl(rows);
  }
}
