import { Module } from '@nestjs/common';
import { DynamicDatabaseService } from './dynamic-database.service';

@Module({
  providers: [DynamicDatabaseService],
  exports: [DynamicDatabaseService],
})
export class DynamicDatabaseModule {}
