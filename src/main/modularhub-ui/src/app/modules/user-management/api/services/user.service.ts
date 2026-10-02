import {Injectable} from '@angular/core';
import {User, UserRequest} from 'app/modules/user-management/api/models/user';
import {HttpClient} from '@angular/common/http';
import {UserCriteria} from 'app/modules/user-management/api/user-criteria';
import {environment} from 'environments/environment';
import {forkJoin} from 'rxjs';
import {UUID} from 'app/core/api/models/uuid';

@Injectable({
  providedIn: 'root',
})
export class UserService {

  private readonly resourceUrl: string;

  constructor(private readonly http: HttpClient) {
    this.resourceUrl = `${environment.apiUrl}/users`;
  }

  getUsers(criteria: UserCriteria) {
    const params = criteria.toParams();
    return this.http.get<User[]>(`${this.resourceUrl}`, {
      params,
      observe: 'response'
    });
  }

  getUserById(id: string) {
    return this.http.get<User>(`${this.resourceUrl}/${id}`);
  }

  createUser(user: UserRequest) {
    return this.http.post<User>(`${this.resourceUrl}`, user);
  }

  updateUser(id: string, user: UserRequest) {
    return this.http.put<User>(`${this.resourceUrl}/${id}`, user);
  }

  deleteUsers(usersToDelete: User[]) {
    const deleteObservables = usersToDelete.map(u =>
      this.http.delete<User>(`${this.resourceUrl}/${u.id}`)
    );

    return forkJoin(deleteObservables);
  }

  resetPassword(id: UUID) {
    return this.http.post<void>(`${this.resourceUrl}/${id}/reset-password`, null);
  }
}
