import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Group} from 'app/core/api/models/group';
import {environment} from 'environments/environment';

@Injectable({
  providedIn: 'root',
})
export class GroupService {

  private readonly resourceUrl: string;

  constructor(private readonly http: HttpClient) {
    this.resourceUrl = `${environment.apiUrl}/groups`;
  }

  getGroups() {
    return this.http.get<Group[]>(`${this.resourceUrl}`);
  }

}
