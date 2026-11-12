import {Injectable} from '@angular/core';
import {LanguageApiService} from 'app/core/api/generated';

@Injectable({
  providedIn: 'root',
})
export class LanguageService {

  constructor(private readonly languageApi: LanguageApiService) {}

  getLanguages() {
    return this.languageApi.getLanguages();
  }
}
