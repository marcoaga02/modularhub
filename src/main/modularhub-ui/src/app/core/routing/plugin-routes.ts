import { EnvironmentProviders, inject, provideAppInitializer } from '@angular/core';
import { Route, Router, Routes } from '@angular/router';
import { firstValueFrom, retry } from 'rxjs';
import { Plugin } from 'app/core/api/generated';
import { AccountService } from 'app/core/api/services/account.service';
import { PluginService } from 'app/core/api/services/plugin.service';
import { AuthService } from 'app/core/auth/auth.service';
import { MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS } from 'app/core/utils/constants/retry';
import { PLUGIN_REGISTRY } from 'app/plugin-registry';

export const FALLBACK_PATH = 'dashboard';

export function providePluginRoutes(): EnvironmentProviders {
  return provideAppInitializer(async () => {
    const authService = inject(AuthService);
    const accountService = inject(AccountService);
    const pluginService = inject(PluginService);
    const router = inject(Router);

    await authService.whenAuthenticated();
    await accountService.whenLoaded();

    const plugins = await firstValueFrom(
      pluginService.getPlugins().pipe(retry({ count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS })),
    );

    router.resetConfig(buildRoutes(plugins));
  });
}

export function buildRoutes(
  plugins: Plugin[],
  registry: Record<string, Route['loadComponent']> = PLUGIN_REGISTRY,
): Routes {
  const defaultPath = plugins[0]?.path ?? FALLBACK_PATH;
  const pluginRoutes: Routes = plugins
    .filter((p) => registry[p.path])
    .map((p) => ({
      path: p.path,
      loadComponent: registry[p.path],
      data: { title: p.description },
    }));

  return [
    { path: '', redirectTo: defaultPath, pathMatch: 'full' },
    ...pluginRoutes,
    { path: '**', redirectTo: defaultPath },
  ];
}
