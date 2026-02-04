import { Control } from '../entities/control.entity';
import { CreateControlDto } from '../dto/inputs/CreateControlDto';
import { ControlResponseDto } from '../dto/outputs/ControlResponseDto';

export class ControlMapper {
  static toEntity(dto: CreateControlDto): Control {
    const control = new Control();
    control.id_number = dto.id_number;
    control.name = dto.name;
    control.type = dto.type;
    control.description = dto.description;

    return control;
  }

  static toResponseDto(entity: Control): ControlResponseDto {
    const response = new ControlResponseDto();
    response.id_number = entity.id_number;
    response.name = entity.name;
    response.type = entity.type;
    response.description = entity.description;

    return response;
  }
}
