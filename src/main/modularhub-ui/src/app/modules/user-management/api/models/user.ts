import {Gender} from 'app/core/api/models/gender';
import {Language} from 'app/core/api/models/language';
import {Group} from 'app/core/api/models/group';
import {UUID} from 'app/core/api/models/uuid';
import {Audit} from 'app/core/api/models/audit';

export interface User {
  id?: UUID,
  firstname: string,
  lastname: string,
  gender: Gender,
  language?: Language,
  mobileNumber?: string,
  taxIdNumber: string,

  email: string,
  username: string,
  groups: Group[],
  enabled: boolean,
  audit?: Audit
}

export interface UserRequest {
  firstname: string,
  lastname: string,
  gender: Gender,
  languageId: UUID,
  mobileNumber?: string,
  taxIdNumber: string,

  email: string,
  username: string,
  password?: string,
  groupIds: UUID[],
  enabled: boolean,
}
