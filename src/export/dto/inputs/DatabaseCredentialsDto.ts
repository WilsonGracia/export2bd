import { IsString, IsNumber, IsOptional, Min, Max } from 'class-validator';

export class DatabaseCredentialsDto {
  @IsString()
  host: string;

  @IsNumber()
  @Min(1)
  @Max(65535)
  port: number;

  @IsString()
  username: string;

  @IsString()
  password: string;

  @IsString()
  database: string;

  @IsOptional()
  @IsString()
  schema?: string;
}
