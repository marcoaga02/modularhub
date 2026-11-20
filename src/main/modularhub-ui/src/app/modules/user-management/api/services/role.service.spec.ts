import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {firstValueFrom} from 'rxjs';

import {RoleService} from 'app/modules/user-management/api/services/role.service';
import {provideApi, RoleSummary} from 'app/modules/user-management/api/generated';
import {UUID} from 'app/core/api/models/uuid';

describe('RoleService', () => {
  const apiUrl = 'http://test/api';
  const role: RoleSummary = {
    id: 'role-id' as UUID,
    name: 'Operators',
    system: false,
  };

  let service: RoleService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideApi(apiUrl)],
    });
    service = TestBed.inject(RoleService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should get the roles from the roles endpoint', async () => {
    const result = firstValueFrom(service.getRoles());

    const call = httpTesting.expectOne(`${apiUrl}/roles`);
    expect(call.request.method).toBe('GET');
    call.flush([role]);

    expect(await result).toEqual([role]);
  });
});
