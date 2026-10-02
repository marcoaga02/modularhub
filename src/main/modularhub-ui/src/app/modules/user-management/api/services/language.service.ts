import {Injectable} from '@angular/core';
import {Language} from 'app/core/api/models/language';
import {HttpClient} from '@angular/common/http';
import {environment} from 'environments/environment';

@Injectable({
  providedIn: 'root',
})
export class LanguageService {

  private readonly resourceUrl: string;

  constructor(
    private readonly http: HttpClient,
  ) {
    this.resourceUrl = `${environment.apiUrl}/languages`;
  }

  getLanguages() {
    return this.http.get<Language[]>(`${this.resourceUrl}`);
  }
}
