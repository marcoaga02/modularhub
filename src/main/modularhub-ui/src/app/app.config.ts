import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { ConfirmationService, MessageService } from 'primeng/api';
import { provideOAuthClient } from 'angular-oauth2-oidc';
import { environment } from 'environments/environment';
import { provideApi } from 'app/core/api/generated';
import { provideApi as provideUserManagementApi } from 'app/modules/user-management/api/generated';
import { registerLocaleData } from '@angular/common';
import localeIt from '@angular/common/locales/it';
import localeEn from '@angular/common/locales/en';
import { DEFAULT_LANGUAGE } from 'app/core/utils/constants/languages';
import { provideAuthInitializer } from 'app/core/auth/auth-initializer';
import { providePluginRoutes } from 'app/core/routing/plugin-routes';
import { oAuthInterceptor } from 'app/core/api/interceptors/oauth.interceptor';
import { errorHandlerInterceptor } from 'app/core/api/interceptors/error-handler.interceptor';

// TODO registrare i nuovi locale
registerLocaleData(localeIt, 'it');
registerLocaleData(localeEn, 'en');

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideAuthInitializer(),
    providePluginRoutes(),
    providePrimeNG({
      theme: {
        preset: Aura,
      },
    }),
    provideHttpClient(withInterceptors([oAuthInterceptor, errorHandlerInterceptor])),
    provideOAuthClient(),
    provideApi(environment.apiUrl),
    provideUserManagementApi(environment.apiUrl),
    provideTranslateService({
      loader: provideTranslateHttpLoader({
        prefix: './i18n/',
        suffix: '.json',
      }),
      fallbackLang: DEFAULT_LANGUAGE,
      lang: DEFAULT_LANGUAGE,
    }),
    MessageService,
    ConfirmationService,
  ],
};
