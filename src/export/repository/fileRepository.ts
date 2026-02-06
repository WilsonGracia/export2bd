import { Injectable } from '@nestjs/common';
import { EntityManager, Repository } from 'typeorm';
import { Control } from '../entities/control.entity';

@Injectable()
export class fileRepository extends Repository<Control> {
  async insertControl(control: Control, manager: EntityManager): Promise<void> {
    await manager.upsert(Control, control, ['id_number']);
  }
}
