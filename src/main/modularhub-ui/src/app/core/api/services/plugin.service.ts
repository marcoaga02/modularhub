import {Injectable, signal} from '@angular/core';
import {Observable, tap} from 'rxjs';
import {Plugin, PluginApiService} from 'app/core/api/generated';

@Injectable({
  providedIn: 'root',
})
export class PluginService {

  private readonly pluginsSignal = signal<Plugin[]>([]);
  readonly plugins = this.pluginsSignal.asReadonly();

  constructor(private readonly pluginApi: PluginApiService) {}

  getPlugins(): Observable<Plugin[]> {
    return this.pluginApi.getAuthorizedPlugins().pipe(
      tap(plugins => this.pluginsSignal.set(plugins))
    );
  }

}
