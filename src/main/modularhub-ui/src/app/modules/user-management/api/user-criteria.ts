import {PagedCriteria} from 'app/core/api/criteria';
import {UserSortField} from 'app/modules/user-management/api/generated';

type UserFilter = {
  text?: string;
}

export class UserCriteria extends PagedCriteria {
  override filter: UserFilter;

  constructor(offset: number = 0, limit: number = 0, filter: UserFilter = {}, sort: string = UserSortField.Lastname) {
    super(offset, limit, sort);
    this.filter = filter;
  }

}
