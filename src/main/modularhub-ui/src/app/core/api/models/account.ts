import {AccountPreferences} from 'app/core/api/models/account-preferences';
import {Role} from 'app/core/api/models/role';
import {Gender} from 'app/core/api/models/gender';

export interface Account {
  identityId: string,
  email: string,
  username: string,
  firstName: string,
  lastName: string,
  roles: Role[],

  preferences: AccountPreferences,

  gender: Gender,
  mobileNumber: string,
  taxIdNumber: string
}
