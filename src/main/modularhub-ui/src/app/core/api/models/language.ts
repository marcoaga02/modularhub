import {UUID} from 'app/core/api/models/uuid';

export interface Language {
  id: UUID,
  code: string,
  label: string,
  isDefault: boolean,
}
