import { inject, Injectable } from '@angular/core';
import { RoleApiService } from 'app/modules/user-management/api/generated';

@Injectable({
  providedIn: 'root',
})
export class RoleService {
  private readonly roleApi = inject(RoleApiService);

  getRoles() {
    return this.roleApi.getAllRoles();
  }
}
