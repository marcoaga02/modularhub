import { inject, Injectable } from '@angular/core';
import { UserCriteria } from 'app/modules/user-management/api/user-criteria';
import {
  UserApiService,
  UserCreateRequest,
  UserUpdateRequest,
} from 'app/modules/user-management/api/generated';
import { UUID } from 'app/core/api/models/uuid';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly userApi = inject(UserApiService);

  getUsers(criteria: UserCriteria) {
    return this.userApi.getAllUsers(
      criteria.filter.text,
      criteria.page,
      criteria.size,
      criteria.sort ? [criteria.sort] : undefined,
      'response',
    );
  }

  getUserById(id: UUID) {
    return this.userApi.getUserByUuid(id);
  }

  createUser(user: UserCreateRequest) {
    return this.userApi.createUser(user);
  }

  updateUser(id: UUID, user: UserUpdateRequest) {
    return this.userApi.updateUser(id, user);
  }

  deleteUser(id: UUID) {
    return this.userApi.deleteUserByUuid(id);
  }

  resetPassword(id: UUID) {
    return this.userApi.resetPassword(id);
  }
}
