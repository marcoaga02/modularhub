import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Plugin, PluginApiService } from 'app/core/api/generated';

@Injectable({
  providedIn: 'root',
})
export class PluginService {
  private readonly pluginApi = inject(PluginApiService);

  private readonly pluginsSignal = signal<Plugin[]>([]);
  readonly plugins = this.pluginsSignal.asReadonly();

  getPlugins(): Observable<Plugin[]> {
    return this.pluginApi
      .getAuthorizedPlugins()
      .pipe(tap((plugins) => this.pluginsSignal.set(plugins)));
  }
}
