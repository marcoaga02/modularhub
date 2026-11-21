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

  function pluginLinks(): HTMLAnchorElement[] {
    return Array.from(element.querySelectorAll<HTMLAnchorElement>('nav a'));
  }

  function button(icon: string): HTMLButtonElement {
    return element.querySelector<HTMLElement>(`nav button i.${icon}`)!.closest('button')!;
  }

  async function click(target: HTMLElement) {
    target.click();
    await fixture.whenStable();
  }

  it('should start open with a link and a description for every plugin', () => {
    expect(pluginLinks().map((link) => link.getAttribute('href'))).toEqual(['/users', '/reports']);
    expect(element.textContent).toContain('ModularHub');
    expect(element.textContent).toContain('users.description');
    expect(element.textContent).toContain('reports.description');
  });

  it('should show only the icons when it is collapsed', async () => {
    await click(button('pi-angle-double-left'));

    expect(pluginLinks()).toHaveLength(2);
    expect(element.textContent).not.toContain('users.description');
    expect(element.textContent).not.toContain('ModularHub');
  });

  it('should show the descriptions again when it is expanded', async () => {
    await click(button('pi-angle-double-left'));

    await click(button('pi-angle-double-right'));

    expect(element.textContent).toContain('users.description');
  });

  it('should navigate to the plugin and highlight its link', async () => {
    await click(pluginLinks()[1]);

    expect(TestBed.inject(Router).url).toBe('/reports');
    expect(pluginLinks()[1].classList).toContain('bg-highlight');
    expect(pluginLinks()[0].classList).not.toContain('bg-highlight');
  });

  it('should log out only after the confirmation is accepted', async () => {
    await click(button('pi-sign-out'));

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
