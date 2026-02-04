import { Body, Controller, Post } from '@nestjs/common';
import { ExportService } from './export.service';
import { CreateControlDto } from './dto/inputs/CreateControlDto';
import { ControlResponseDto } from './dto/outputs/ControlResponseDto';

@Controller('export')
export class ExportController {
  constructor(private readonly exportService: ExportService) {}

  @Post('control')
  async createControl(
    @Body() dto: CreateControlDto,
  ): Promise<ControlResponseDto> {
    return this.exportService.createControl(dto);
  }
}
