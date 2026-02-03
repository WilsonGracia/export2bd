import { Controller, Get } from '@nestjs/common';
import { DatabaseService } from './database.service';

@Controller('database')
export class DatabaseController {

    constructor(private readonly dbService: DatabaseService ){}


    @Get('/test')
    async testDb(){
            return this.dbService.testConnection();
    }

    

}
