import {UUID} from 'app/core/api/models/uuid';

export interface Group {
  id: UUID,
  name: string,
  description: string,
}
