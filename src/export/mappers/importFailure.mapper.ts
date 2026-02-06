import { ImportFailureDto } from '../dto/outputs/ImportFailureDto';

export class ImportFailureMapper {
  static toDto(
    rowNumber: number,
    row: Record<string, any>,
    error: unknown,
  ): ImportFailureDto {
    const values = Object.values(row);

    const dto = new ImportFailureDto();
    dto.row = rowNumber;
    dto.id_number = values[0] ? String(values[0]).trim() : undefined;
    dto.reason = error instanceof Error ? error.message : 'Unexpected error';
    dto.raw = row;

    return dto;
  }
}
