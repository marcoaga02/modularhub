import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { SidebarComponent } from 'app/core/components/sidebar/sidebar.component';
import { PluginService } from 'app/core/api/services/plugin.service';
import { AccountService } from 'app/core/api/services/account.service';
import { Plugin } from 'app/core/api/generated';
import { ConfirmDialogService } from 'app/core/utils/services/confirm-dialog.service';

@Component({ template: '' })
class DummyComponent {}

describe('SidebarComponent', () => {
  const usersPlugin: Plugin = {
    path: 'users',
    name: 'users',
    description: 'users.description',
    icon: 'pi pi-users',
  };
  const reportsPlugin: Plugin = {
    path: 'reports',
    name: 'reports',
    description: 'reports.description',
    icon: 'pi pi-chart-bar',
  };

  let fixture: ComponentFixture<SidebarComponent>;
  let element: HTMLElement;
  let accountService: { logout: ReturnType<typeof vi.fn> };
  let confirmDialog: { confirm: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    accountService = { logout: vi.fn() };
    confirmDialog = { confirm: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [SidebarComponent],
      providers: [
        provideTranslateService(),
        provideRouter([
          { path: 'users', component: DummyComponent },
          { path: 'reports', component: DummyComponent },
        ]),
        { provide: ConfirmDialogService, useValue: confirmDialog },
        { provide: PluginService, useValue: { plugins: signal([usersPlugin, reportsPlugin]) } },
        { provide: AccountService, useValue: accountService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SidebarComponent);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  function pluginItems(): HTMLElement[] {
    return Array.from(element.querySelectorAll<HTMLElement>('[role="button"]'));
  }

  async function toggle() {
    element.querySelector<HTMLButtonElement>('p-button button')!.click();
    await fixture.whenStable();
  }

  function isOpen(): boolean {
    return element.firstElementChild!.classList.contains('w-80');
  }

  it('should show one item for every plugin, closed and without descriptions', () => {
    expect(pluginItems()).toHaveLength(2);
    expect(isOpen()).toBe(false);
    expect(element.textContent).not.toContain('users.description');
  });

  it('should show the plugin descriptions when it is opened', async () => {
    await toggle();

    expect(isOpen()).toBe(true);
    expect(element.textContent).toContain('users.description');
    expect(element.textContent).toContain('reports.description');
  });

  it('should close when it is toggled again', async () => {
    await toggle();
    await toggle();

    expect(isOpen()).toBe(false);
  });

  it('should navigate to the plugin and close when a plugin is clicked', async () => {
    await toggle();

    pluginItems()[1].click();
    await fixture.whenStable();

    expect(TestBed.inject(Router).url).toBe('/reports');
    expect(isOpen()).toBe(false);
  });

  it('should navigate to the plugin and stay closed when it is already closed', async () => {
    pluginItems()[0].click();
    await fixture.whenStable();

    expect(TestBed.inject(Router).url).toBe('/users');
    expect(isOpen()).toBe(false);
  });

  it('should log out only after the confirmation is accepted', async () => {
    element.querySelector<HTMLElement>('.text-red-500')!.click();
    await fixture.whenStable();

    expect(confirmDialog.confirm).toHaveBeenCalledWith(
      'general.logout.title',
      'general.logout.message',
      expect.any(Function),
    );
    expect(accountService.logout).not.toHaveBeenCalled();

    confirmDialog.confirm.mock.lastCall![2]();

    expect(accountService.logout).toHaveBeenCalledOnce();
  });
});
