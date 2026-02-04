import { Injectable } from '@nestjs/common';
import { DataSource } from 'typeorm';

@Injectable()
export class DatabaseService {
  constructor(private readonly dataSource: DataSource) {}

  async testConnection(): Promise<string> {
    // ! just testing db connection
    const result = await this.dataSource.query('SELECT 1 AS ok');
    return result[0].ok === 1 ? 'Connected to DB' : 'Not connected';
  }
}
