import { ApplicationInitStatus, Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Route, Router } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from 'app/core/auth/auth.service';
import { Plugin } from 'app/core/api/generated';
import { AccountService } from 'app/core/api/services/account.service';
import { PluginService } from 'app/core/api/services/plugin.service';
import { buildRoutes, FALLBACK_PATH, providePluginRoutes } from 'app/core/routing/plugin-routes';

@Component({ template: '' })
class DummyComponent {}

describe('plugin routes', () => {
  const users: Plugin = {
    path: 'users',
    name: 'users',
    description: 'users.description',
    icon: 'pi pi-users',
  };
  const reports: Plugin = {
    path: 'reports',
    name: 'reports',
    description: 'reports.description',
    icon: 'pi pi-chart-bar',
  };
  const unknown: Plugin = {
    path: 'unknown',
    name: 'unknown',
    description: 'unknown.description',
    icon: 'pi pi-question',
  };

  const loadDummy = () => Promise.resolve(DummyComponent);
  const registry: Record<string, Route['loadComponent']> = {
    users: loadDummy,
    reports: loadDummy,
  };

  describe('buildRoutes', () => {
    it('should create a lazy route for every registered plugin', () => {
      const routes = buildRoutes([users, reports], registry);

      expect(routes.slice(1, -1)).toEqual([
        { path: 'users', loadComponent: loadDummy, data: { title: 'users.description' } },
        { path: 'reports', loadComponent: loadDummy, data: { title: 'reports.description' } },
      ]);
    });

    it('should skip the plugins without a registered component', () => {
      const routes = buildRoutes([unknown, users], registry);

      expect(routes.map((r) => r.path)).toEqual(['', 'users', '**']);
    });

    it('should redirect the empty and unknown paths to the first plugin', () => {
      const routes = buildRoutes([reports, users], registry);

      expect(routes[0]).toEqual({ path: '', redirectTo: 'reports', pathMatch: 'full' });
      expect(routes[routes.length - 1]).toEqual({ path: '**', redirectTo: 'reports' });
    });

    it('should redirect to the fallback path when there are no plugins', () => {
      const routes = buildRoutes([], registry);

      expect(routes).toEqual([
        { path: '', redirectTo: FALLBACK_PATH, pathMatch: 'full' },
        { path: '**', redirectTo: FALLBACK_PATH },
      ]);
    });
  });

  describe('providePluginRoutes', () => {
    it('should load the plugins after the account and install their routes', async () => {
      const userManagement: Plugin = { ...users, path: 'user-management' };
      const authService = { whenAuthenticated: vi.fn().mockResolvedValue(undefined) };
      const accountService = { whenLoaded: vi.fn().mockResolvedValue(undefined) };
      const pluginService = { getPlugins: vi.fn().mockReturnValue(of([userManagement, unknown])) };

      TestBed.configureTestingModule({
        providers: [
          provideRouter([]),
          providePluginRoutes(),
          { provide: AuthService, useValue: authService },
          { provide: AccountService, useValue: accountService },
          { provide: PluginService, useValue: pluginService },
        ],
      });
      await TestBed.inject(ApplicationInitStatus).donePromise;

      expect(authService.whenAuthenticated.mock.invocationCallOrder[0]).toBeLessThan(
        accountService.whenLoaded.mock.invocationCallOrder[0],
      );
      expect(accountService.whenLoaded.mock.invocationCallOrder[0]).toBeLessThan(
        pluginService.getPlugins.mock.invocationCallOrder[0],
      );
      expect(TestBed.inject(Router).config.map((r) => r.path)).toEqual([
        '',
        'user-management',
        '**',
      ]);
    });
  });
});
