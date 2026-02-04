import { ConflictException, Injectable } from '@nestjs/common';
import { Control } from './entities/control.entity';
import { CreateControlDto } from './dto/inputs/CreateControlDto';
import { ControlResponseDto } from './dto/outputs/ControlResponseDto';
import { DataSource, EntityManager } from 'typeorm';
import { ControlMapper } from './mappers/control.mapper';

@Injectable()
export class ExportService {
  constructor(private readonly dataSource: DataSource) {}

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
}
