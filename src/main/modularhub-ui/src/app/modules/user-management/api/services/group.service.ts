import {Injectable} from '@angular/core';
import {GroupApiService} from 'app/core/api/generated';

@Injectable({
  providedIn: 'root',
})
export class GroupService {

  constructor(private readonly groupApi: GroupApiService) {}

  getGroups() {
    return this.groupApi.getAllGroups();
  }

}
