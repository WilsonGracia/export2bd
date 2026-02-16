import { Controller } from '@nestjs/common';
import { DynamicDatabaseService } from './dynamic-database.service';

@Controller('dynamic-database')
export class DynamicDatabaseController {
  constructor(private readonly dynamicDatabaseService: DynamicDatabaseService) {}
}
