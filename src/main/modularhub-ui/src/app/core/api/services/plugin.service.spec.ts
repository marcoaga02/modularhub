import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {firstValueFrom} from 'rxjs';

import {PluginService} from 'app/core/api/services/plugin.service';
import {Plugin, provideApi} from 'app/core/api/generated';

describe('PluginService', () => {
  const apiUrl = 'http://test/api';
  let service: PluginService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideApi(apiUrl)],
    });
    service = TestBed.inject(PluginService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should get the plugins and store them in the signal', async () => {
    const plugins: Plugin[] = [
      {path: 'user-management', name: 'name', description: 'description', icon: 'pi pi-users'},
    ];

    const result = firstValueFrom(service.getPlugins());

    const request = httpTesting.expectOne(`${apiUrl}/plugins`);
    expect(request.request.method).toBe('GET');
    request.flush(plugins);

    expect(await result).toEqual(plugins);
    expect(service.plugins()).toEqual(plugins);
  });
});
