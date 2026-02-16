import { Injectable, BadRequestException } from '@nestjs/common';
import { DataSource, DataSourceOptions } from 'typeorm';
import { DatabaseCredentialsDto } from '../export/dto/inputs/DatabaseCredentialsDto';
import { Control } from '../export/entities/control.entity';

@Injectable()
export class DynamicDatabaseService {
  private connectionCache = new Map<string, DataSource>();
  private readonly MAX_CONNECTIONS = 50;
  private readonly CONNECTION_TIMEOUT = 30 * 60 * 1000;

  async getDataSource(
    credentials: DatabaseCredentialsDto,
  ): Promise<DataSource> {
    // Validar que las credenciales existan
    if (!credentials) {
      throw new BadRequestException('Database credentials are required');
    }

    this.validateCredentialsObject(credentials);

    const cacheKey = this.generateCacheKey(credentials);

    if (this.connectionCache.has(cacheKey)) {
      const cachedDataSource = this.connectionCache.get(cacheKey)!;

      if (cachedDataSource.isInitialized) {
        return cachedDataSource;
      } else {
        this.connectionCache.delete(cacheKey);
      }
    }

    if (this.connectionCache.size >= this.MAX_CONNECTIONS) {
      await this.cleanOldConnections();
    }

    const dataSource = await this.createDataSource(credentials);
    this.connectionCache.set(cacheKey, dataSource);

    setTimeout(() => {
      this.closeConnection(cacheKey);
    }, this.CONNECTION_TIMEOUT);

    return dataSource;
  }

  private validateCredentialsObject(credentials: DatabaseCredentialsDto): void {
    const requiredFields = ['host', 'port', 'username', 'password', 'database'];
    const missingFields = requiredFields.filter((field) => !credentials[field]);

    if (missingFields.length > 0) {
      throw new BadRequestException(
        `Missing required credential fields: ${missingFields.join(', ')}`,
      );
    }
  }

  private async createDataSource(
    credentials: DatabaseCredentialsDto,
  ): Promise<DataSource> {
    try {
      const options: DataSourceOptions = {
        type: 'postgres',
        host: credentials.host,
        port: credentials.port,
        username: credentials.username,
        password: credentials.password,
        database: credentials.database,
        schema: credentials.schema || 'public',
        entities: [Control],
        synchronize: false,
        // logging: true,
        logging: false,
        connectTimeoutMS: 10000,
        extra: {
          max: 10,
          min: 2,
          idleTimeoutMillis: 30000,
        },
      };

      const dataSource = new DataSource(options);

      await dataSource.initialize();

      return dataSource;
    } catch (error) {
      throw new BadRequestException(
        `Failed to connect to database: ${error.message}`,
      );
    }
  }

  private generateCacheKey(credentials: DatabaseCredentialsDto): string {
    return `${credentials.username}@${credentials.host}:${credentials.port}/${credentials.database}`;
  }

  private async closeConnection(cacheKey: string): Promise<void> {
    const dataSource = this.connectionCache.get(cacheKey);
    if (dataSource && dataSource.isInitialized) {
      try {
        await dataSource.destroy();
      } catch (error) {
        console.error(`Error closing connection ${cacheKey}:`, error);
      }
    }
    this.connectionCache.delete(cacheKey);
  }

  private async cleanOldConnections(): Promise<void> {
    const connectionsToRemove = Math.ceil(this.MAX_CONNECTIONS * 0.2);
    const keys = Array.from(this.connectionCache.keys());

    for (let i = 0; i < connectionsToRemove && i < keys.length; i++) {
      await this.closeConnection(keys[i]);
    }
  }

  async closeAllConnections(): Promise<void> {
    const closePromises = Array.from(this.connectionCache.keys()).map((key) =>
      this.closeConnection(key),
    );
    await Promise.all(closePromises);
  }

  async validateCredentials(
    credentials: DatabaseCredentialsDto,
  ): Promise<boolean> {
    //! if no credentials provided, return false immediately
    if (!credentials) {
      return false;
    }

    try {
      this.validateCredentialsObject(credentials);
    } catch (error) {
      return false;
    }

    try {
      const dataSource = await this.createDataSource(credentials);
      await dataSource.query('SELECT 1');
      await dataSource.destroy();
      return true;
    } catch (error) {
      return false;
    }
  }

  getStats() {
    return {
      activeConnections: this.connectionCache.size,
      maxConnections: this.MAX_CONNECTIONS,
      connectionTimeout: this.CONNECTION_TIMEOUT,
    };
  }
}
