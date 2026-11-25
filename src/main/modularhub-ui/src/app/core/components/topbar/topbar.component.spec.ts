import { Component, Signal, signal, WritableSignal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { DialogService } from 'primeng/dynamicdialog';
import { TopbarComponent } from 'app/core/components/topbar/topbar.component';
import { PluginService } from 'app/core/api/services/plugin.service';
import { AccountService } from 'app/core/api/services/account.service';
import { AuthService } from 'app/core/auth/auth.service';
import { Account, Language, Plugin } from 'app/core/api/generated';
import { AccountDetailComponent } from 'app/core/pages/account-detail/account-detail.component';
import { FORM_DIALOG_CONFIG } from 'app/core/utils/constants/dialog';

@Component({ template: '' })
class DummyComponent {}

describe('TopbarComponent', () => {
  const account: Account = {
    identityId: 'identity-id',
    email: 'mario@test.it',
    username: 'mario',
    firstName: 'mario',
    lastName: 'rossi',
    permissions: [],
    preferences: { language: Language.It },
  };
  const usersPlugin: Plugin = {
    path: 'user-management',
    name: 'users',
    description: 'plugins.user-management.description',
    icon: 'pi pi-users',
  };

  let fixture: ComponentFixture<TopbarComponent>;
  let element: HTMLElement;
  let accountSignal: WritableSignal<Account | null>;
  let accountService: { account: Signal<Account | null> };
  let authService: { logout: ReturnType<typeof vi.fn> };
  let dialogService: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    accountSignal = signal<Account | null>(null);
    accountService = { account: accountSignal.asReadonly() };
    authService = { logout: vi.fn() };
    dialogService = { open: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [TopbarComponent],
      providers: [
        provideTranslateService(),
        provideRouter([{ path: 'user-management', component: DummyComponent }]),
        { provide: PluginService, useValue: { plugins: signal([usersPlugin]) } },
        { provide: AccountService, useValue: accountService },
        { provide: AuthService, useValue: authService },
        { provide: DialogService, useValue: dialogService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TopbarComponent);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  async function publishAccount() {
    accountSignal.set(account);
    await fixture.whenStable();
  }

  async function openMenu() {
    element.querySelector<HTMLElement>('p-avatar')!.click();
    await fixture.whenStable();
  }

  async function clickMenuItem(label: string) {
    const items = Array.from(element.querySelectorAll<HTMLElement>('p-popover button'));
    items.find((item) => item.textContent?.includes(label))!.click();
    await fixture.whenStable();
  }

  it('should not show the avatar until the account is loaded', () => {
    expect(element.querySelector('p-avatar')).toBeNull();
  });

  it('should show the initials of the account', async () => {
    await publishAccount();

    expect(element.querySelector('p-avatar')!.textContent?.trim()).toBe('MR');
  });

  it('should show full name and email in the menu', async () => {
    await publishAccount();
    await openMenu();

    const menu = element.querySelector('p-popover')!.textContent;
    expect(menu).toContain('mario rossi');
    expect(menu).toContain('mario@test.it');
  });

  it('should show the description of the plugin of the current route', async () => {
    await TestBed.inject(Router).navigateByUrl('/user-management?page=1');
    await fixture.whenStable();

    expect(element.textContent).toContain('plugins.user-management.description');
  });

  it('should open the account detail dialog from the profile item', async () => {
    await publishAccount();
    await openMenu();

    await clickMenuItem('core.components.top-bar.profile');

    expect(dialogService.open).toHaveBeenCalledWith(
      AccountDetailComponent,
      expect.objectContaining({ ...FORM_DIALOG_CONFIG, width: '32rem' }),
    );
  });

  it('should log out right away', async () => {
    await publishAccount();
    await openMenu();

    await clickMenuItem('general.logout.label');

    expect(authService.logout).toHaveBeenCalledOnce();
  });
});
