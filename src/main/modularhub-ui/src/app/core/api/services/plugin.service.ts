import {Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Plugin} from 'app/core/api/models/plugin'
import {Observable, tap} from 'rxjs';
import {environment} from 'environments/environment';

@Injectable({
  providedIn: 'root',
})
export class PluginService {

  private readonly resourceUrl: string;
  private readonly pluginsSignal = signal<Plugin[]>([]);
  readonly plugins = this.pluginsSignal.asReadonly();

  constructor(private readonly http: HttpClient) {
    this.resourceUrl = `${environment.apiUrl}/plugins`;
  }

  getPlugins(): Observable<Plugin[]> {
    return this.http.get<Plugin[]>(this.resourceUrl).pipe(
      tap(plugins => this.pluginsSignal.set(plugins))
    );
  }

}
