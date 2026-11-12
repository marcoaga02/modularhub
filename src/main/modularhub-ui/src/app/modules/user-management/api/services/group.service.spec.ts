import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {firstValueFrom} from 'rxjs';

import {GroupService} from 'app/modules/user-management/api/services/group.service';
import {IdentityGroup, provideApi} from 'app/core/api/generated';
import {UUID} from 'app/core/api/models/uuid';

describe('GroupService', () => {
  const apiUrl = 'http://test/api';
  let service: GroupService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideApi(apiUrl)],
    });
    service = TestBed.inject(GroupService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should get the groups from the groups endpoint', async () => {
    const groups: IdentityGroup[] = [{id: 'group-id' as UUID, name: 'ADMIN'}];

    const result = firstValueFrom(service.getGroups());

    const request = httpTesting.expectOne(`${apiUrl}/groups`);
    expect(request.request.method).toBe('GET');
    request.flush(groups);

    expect(await result).toEqual(groups);
  });
});
