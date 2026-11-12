import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {firstValueFrom} from 'rxjs';

import {LanguageService} from 'app/modules/user-management/api/services/language.service';
import {LanguageResponse, provideApi} from 'app/core/api/generated';
import {UUID} from 'app/core/api/models/uuid';

describe('LanguageService', () => {
  const apiUrl = 'http://test/api';
  let service: LanguageService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideApi(apiUrl)],
    });
    service = TestBed.inject(LanguageService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get the languages from the languages endpoint', async () => {
    const languages: LanguageResponse[] = [
      {id: 'uuid' as UUID, code: 'it-IT', label: 'Italiano', isDefault: true},
    ];

    const result = firstValueFrom(service.getLanguages());

    const request = httpTesting.expectOne(`${apiUrl}/languages`);
    expect(request.request.method).toBe('GET');
    request.flush(languages);

    expect(await result).toEqual(languages);
  });
});
