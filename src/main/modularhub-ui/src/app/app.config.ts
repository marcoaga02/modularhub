import {
  ApplicationConfig,
  EnvironmentProviders,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners
} from '@angular/core';
import {provideRouter, Router} from '@angular/router';

import {routes} from './app.routes';
import {providePrimeNG} from "primeng/config";
import Aura from '@primeuix/themes/aura';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpRequest,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import {catchError, filter, firstValueFrom, Observable, retry, throwError} from 'rxjs';
import {provideTranslateService} from '@ngx-translate/core';
import {provideTranslateHttpLoader} from '@ngx-translate/http-loader';
import {MessageService} from 'primeng/api';
import {PluginService} from 'app/core/api/services/plugin.service';
import {Plugin} from 'app/core/api/models/plugin';
import {PLUGIN_REGISTRY} from 'app/plugin-registry';
import {OAuthService, OAuthStorage, provideOAuthClient} from 'angular-oauth2-oidc';
import {AccountService} from 'app/core/api/services/account.service';
import {environment} from 'environments/environment';
import {CustomError} from 'app/core/api/models/custom-error';
import {registerLocaleData} from '@angular/common';
import localeIt from '@angular/common/locales/it';
import localeEn from '@angular/common/locales/en';

export const MAX_RETRY_ATTEMPTS = 3;
export const RETRY_DELAY_MS = 1000;

// TODO registrare i nuovi locale
registerLocaleData(localeIt, 'it-IT');
registerLocaleData(localeEn, 'en-EN');

export function provideAuthInitializer(): EnvironmentProviders {
  return provideAppInitializer(async () => {
    const oAuthService = inject(OAuthService);
    const accountService = inject(AccountService);

    oAuthService.configure({
      issuer: environment.oidcIssuer,
      redirectUri: environment.oidcRedirectUri,
      clientId: environment.oidcClientId,
      responseType: 'code',
      scope: 'openid profile email offline_access',
    });

    oAuthService.setupAutomaticSilentRefresh();
    await oAuthService.loadDiscoveryDocumentAndLogin();
    await waitForValidToken(oAuthService);

    accountService.load();
  });
}

export function providePluginRoutes(): EnvironmentProviders {
  return provideAppInitializer(async () => {
    const oAuthService = inject(OAuthService);
    const pluginService = inject(PluginService);
    const router = inject(Router);

    await waitForValidToken(oAuthService);

    const plugins = await firstValueFrom(
      pluginService.getPlugins().pipe(
        retry({ count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS })
      )
    );

    buildRoutes(router)(plugins);
  });
}

function waitForValidToken(oAuthService: OAuthService): Promise<void> {
  if (oAuthService.hasValidAccessToken()) {
    return Promise.resolve();
  }

  return new Promise<void>(resolve => {
    oAuthService.events
      .pipe(filter(e => e.type === 'token_received'))
      .subscribe(() => resolve());
  });
}

function oAuthInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
  let token = inject(OAuthStorage).getItem('access_token');
  let header = 'Bearer ' + token;
  let headers = req.headers.set('Authorization', header);

  req = req.clone({headers});
  return next(req);
}

function errorHandlerInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
  const messageService = inject(MessageService);

  return next(req).pipe(
    catchError((e: HttpErrorResponse) => {
      const error = e.error as CustomError;
      const errorCode = error.errorCode ?? 'exception.internal.error';
      const fields = error.fields?.length ? error.fields.join(', ') : undefined;

      messageService.add({
        severity: 'error',
        summary: 'general.error',
        detail: errorCode,
        data: { fields }
      });
      return throwError(() => e);
    })
  );
}

function buildRoutes(router: Router) {
  return (plugins: Plugin[]) => {
    const pluginRoutes = plugins
      .filter(p => PLUGIN_REGISTRY[p.path])
      .map(p => ({
        path: p.path,
        loadComponent: PLUGIN_REGISTRY[p.path],
        data: {title: p.description}
      }));

    router.resetConfig([
      {path: '', redirectTo: plugins[0]?.path ?? 'dashboard', pathMatch: 'full'},
      ...pluginRoutes,
      {path: '**', redirectTo: plugins[0]?.path ?? 'dashboard'}
    ]);
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideAuthInitializer(),
    providePluginRoutes(),
    providePrimeNG({
      theme: {
        preset: Aura
      }
    }),
    provideHttpClient(
      withInterceptors([oAuthInterceptor, errorHandlerInterceptor])
    ),
    provideOAuthClient(),
    provideTranslateService({
      loader: provideTranslateHttpLoader({
        prefix: './i18n/',
        suffix: '.json'
      }),
      fallbackLang: 'it-IT',
      lang: 'it-IT'
    }),
    MessageService,
  ]
};
