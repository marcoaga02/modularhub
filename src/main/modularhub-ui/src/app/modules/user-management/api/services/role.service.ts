import {Injectable} from '@angular/core';
import {RoleApiService} from 'app/modules/user-management/api/generated';

@Injectable({
  providedIn: 'root',
})
export class RoleService {

  constructor(private readonly roleApi: RoleApiService) {}

  getRoles() {
    return this.roleApi.getAllRoles();
  }
}
