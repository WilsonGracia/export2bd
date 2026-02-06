export class ImportFailureDto {
  row!: number;
  id_number?: string;
  reason!: string;
  raw!: Record<string, any>;
}
