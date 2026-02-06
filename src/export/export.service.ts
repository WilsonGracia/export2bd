import { ConflictException, Injectable } from '@nestjs/common';
import { Control } from './entities/control.entity';
import { CreateControlDto } from './dto/inputs/CreateControlDto';
import { ControlResponseDto } from './dto/outputs/ControlResponseDto';
import { DataSource, EntityManager } from 'typeorm';
import { ControlMapper } from './mappers/control.mapper';
import { fileRepository } from './repository/fileRepository';
import { ImportFailureMapper } from './mappers/importFailure.mapper';
import { ImportFailureDto } from './dto/outputs/ImportFailureDto';

@Injectable()
export class ExportService {
  constructor(
    private readonly dataSource: DataSource,
    private readonly repo: fileRepository,
  ) {}

  async createControl(dto: CreateControlDto): Promise<ControlResponseDto> {
    return this.dataSource.manager.transaction(
      async (transactionalEntityManager: EntityManager) => {
        const exist = await transactionalEntityManager.findOne(Control, {
          where: { id_number: dto.id_number },
        });

        if (exist) {
          throw new ConflictException(
            `This control: ${dto.id_number} already exists`,
          );
        }

        const controlEntity = ControlMapper.toEntity(dto);

        const saved = await transactionalEntityManager.save(
          Control,
          controlEntity,
        );

        return ControlMapper.toResponseDto(saved);
      },
    );
  }

  async insertControl(rows: object[]): Promise<{
    processed: number;
    succeeded: number;
    failed: number;
    failures: ImportFailureDto[];
  }> {
    const failures: ImportFailureDto[] = [];
    let succeeded = 0;

    await this.dataSource.transaction(async (manager: EntityManager) => {
      for (let i = 0; i < rows.length; i++) {
        const rowNumber = i + 1;
        const row = rows[i] as Record<string, any>;

        try {
          const values = Object.values(row);

          const dto: CreateControlDto = {
            id_number: String(values[0]).trim(),
            name: String(values[1]).trim(),
            type: String(values[2]).trim(),
            description: String(values[3]).trim(),
          };

          const control = ControlMapper.toEntity(dto);

          await this.repo.insertControl(control, manager);
          succeeded++;
        } catch (error) {
          failures.push(ImportFailureMapper.toDto(rowNumber, row, error));
        }
      }
    });

    return {
      processed: rows.length,
      succeeded,
      failed: failures.length,
      failures,
    };
  }
}
