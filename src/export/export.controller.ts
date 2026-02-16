import {
  BadRequestException,
  Body,
  Controller,
  Post,
  UploadedFile,
  UseGuards,
  UseInterceptors,
} from '@nestjs/common';
import { ExportService } from './export.service';
import { CreateControlDto } from './dto/inputs/CreateControlDto';
import { ControlResponseDto } from './dto/outputs/ControlResponseDto';
import { FileInterceptor } from '@nestjs/platform-express';
import * as XLSX from 'xlsx';
import {
  extractHeaders,
  validateControlHeaders,
} from './helpers/control.helper';
import { ImportFailureDto } from './dto/outputs/ImportFailureDto';
import { JwtAuthGuard, DbCredentials } from '../auth/guards/jwt-auth.guard';
import { DatabaseCredentialsDto } from './dto/inputs/DatabaseCredentialsDto';

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
    const worksheet = workbook.Sheets[workbook.SheetNames[0]];

    const error = validateControlHeaders(extractHeaders(worksheet));
    if (error) throw new BadRequestException(error);

    const rows = XLSX.utils.sheet_to_json<Record<string, any>>(worksheet);
    return await this.exportService.insertControl(rows);
  }
  /*
  @Post('control-with-credentials')
  async createControlWithCredentials(
    @Body()
    body: {
      data: CreateControlDto;
      credentials: DatabaseCredentialsDto;
    },
  ): Promise<ControlResponseDto> {
    return this.exportService.createControlWithCredentials(
      body.data,
      body.credentials,
    );
  }

  */ //! This endpoint is for testing purposes

  @Post('control-with-credentials')
  @UseGuards(JwtAuthGuard)
  async createControlWithCredentials(
    @Body() dto: CreateControlDto,
    @DbCredentials() credentials: DatabaseCredentialsDto,
  ): Promise<ControlResponseDto> {
    return this.exportService.createControlWithCredentials(dto, credentials);
  }
}
