import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {firstValueFrom} from 'rxjs';

import {UserService} from 'app/modules/user-management/api/services/user.service';
import {
  provideApi,
  UserCreateRequest,
  UserResponse,
  UserUpdateRequest
} from 'app/modules/user-management/api/generated';
import {Gender} from 'app/core/api/generated';
import {UserCriteria, UserSortField} from 'app/modules/user-management/api/user-criteria';
import {UUID} from 'app/core/api/models/uuid';
import {HttpHeader} from 'app/core/api/http-header';

describe('UserService', () => {
  const apiUrl = 'http://test/api';
  const userId = 'user-id' as UUID;
  const user: UserResponse = {
    id: userId,
    firstname: 'Mario',
    lastname: 'Rossi',
    gender: Gender.M,
    taxIdNumber: 'TAX',
    email: 'mario@test.it',
    username: 'mario',
    enabled: true,
    groups: [],
  };
  const updateRequest: UserUpdateRequest = {
    firstname: 'Mario',
    lastname: 'Rossi',
    gender: Gender.M,
    languageId: 'lang-id' as UUID,
    taxIdNumber: 'TAX',
    email: 'mario@test.it',
    username: 'mario',
    groupIds: [],
    enabled: true,
  };

  let service: UserService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideApi(apiUrl)],
    });
    service = TestBed.inject(UserService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should send criteria as query params and return the full response', async () => {
    const criteria = new UserCriteria(40, 20, {text: 'mario'}, UserSortField.Firstname);

    const result = firstValueFrom(service.getUsers(criteria));

    const request = httpTesting.expectOne(r => r.url === `${apiUrl}/users`);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('text')).toBe('mario');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('20');
    expect(request.request.params.get('sort')).toBe('firstname');
    request.flush([user], {headers: {[HttpHeader.TotalCount]: '1'}});

    const response = await result;
    expect(response.body).toEqual([user]);
    expect(response.headers.get(HttpHeader.TotalCount)).toBe('1');
  });

  it('should get a user by id', async () => {
    const result = firstValueFrom(service.getUserById(userId));

    const request = httpTesting.expectOne(`${apiUrl}/users/${userId}`);
    expect(request.request.method).toBe('GET');
    request.flush(user);

    expect(await result).toEqual(user);
  });

  it('should create a user', async () => {
    const createRequest: UserCreateRequest = {...updateRequest, password: 'password'};

    const result = firstValueFrom(service.createUser(createRequest));

    const request = httpTesting.expectOne(`${apiUrl}/users`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(createRequest);
    request.flush(user);

    expect(await result).toEqual(user);
  });

  it('should update a user', async () => {
    const result = firstValueFrom(service.updateUser(userId, updateRequest));

    const request = httpTesting.expectOne(`${apiUrl}/users/${userId}`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(updateRequest);
    request.flush(user);

    expect(await result).toEqual(user);
  });

  it('should delete every selected user', async () => {
    const otherId = 'other-id' as UUID;

    const result = firstValueFrom(service.deleteUsers([user, {...user, id: otherId}]));

    httpTesting.expectOne(`${apiUrl}/users/${userId}`).flush(null);
    httpTesting.expectOne(`${apiUrl}/users/${otherId}`).flush(null);

    expect((await result).length).toBe(2);
  });

  it('should reset the password of a user', async () => {
    const result = firstValueFrom(service.resetPassword(userId));

    const request = httpTesting.expectOne(`${apiUrl}/users/${userId}/reset-password`);
    expect(request.request.method).toBe('POST');
    request.flush(null);

    await result;
  });
});
