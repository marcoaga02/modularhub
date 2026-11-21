import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of, Subject, throwError } from 'rxjs';
import { provideTranslateService, TranslateService } from '@ngx-translate/core';
import { registerLocaleData } from '@angular/common';
import localeIt from '@angular/common/locales/it';
import { UserDetailComponent } from 'app/modules/user-management/pages/user-detail/user-detail.component';
import { UserService } from 'app/modules/user-management/api/services/user.service';
import { PasswordResetService } from 'app/modules/user-management/utils/services/password-reset.service';
import { RoleSummary, UserResponse } from 'app/modules/user-management/api/generated';
import { AccountService } from 'app/core/api/services/account.service';
import { Account, Gender, Language } from 'app/core/api/generated';
import { UUID } from 'app/core/api/models/uuid';
import { MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS } from 'app/core/utils/constants/retry';

describe('UserDetailComponent', () => {
  const DETAIL = 'modules.user-management.pages.user-detail';

  const role: RoleSummary = { id: 'role-id' as UUID, name: 'Operators', system: false };
  const user: UserResponse = {
    id: 'user-id' as UUID,
    firstname: 'mario',
    lastname: 'rossi',
    gender: Gender.M,
    language: Language.It,
    mobileNumber: '3331234567',
    taxIdNumber: 'RSSMRA80A01H501U',
    email: 'mario@test.it',
    username: 'mario.rossi',
    enabled: true,
    roles: [role],
  };
  const luigi: UserResponse = { ...user, id: 'luigi-id' as UUID, username: 'luigi.verdi' };
  const loadedUser: UserResponse = {
    ...user,
    audit: {
      createdOn: '2026-01-01T10:00:00Z',
      createdBy: 'Admin',
      updatedOn: '2026-01-02T10:00:00Z',
      updatedBy: 'Luigi Verdi',
    },
  };
  const account: Account = {
    identityId: 'identity-id',
    email: 'admin@test.it',
    username: 'admin',
    firstName: 'Admin',
    lastName: 'Admin',
    permissions: [],
    preferences: { language: Language.En },
  };

  let fixture: ComponentFixture<UserDetailComponent>;
  let element: HTMLElement;
  let userService: { getUserById: ReturnType<typeof vi.fn> };
  let passwordReset: { resetPassword: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    userService = { getUserById: vi.fn().mockReturnValue(of(loadedUser)) };
    passwordReset = { resetPassword: vi.fn() };
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  async function createComponent(signedIn: Account = account) {
    await TestBed.configureTestingModule({
      imports: [UserDetailComponent],
      providers: [
        provideTranslateService({ lang: 'en' }),
        { provide: UserService, useValue: userService },
        { provide: PasswordResetService, useValue: passwordReset },
        { provide: AccountService, useValue: { account: signal(signedIn).asReadonly() } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UserDetailComponent);
    element = fixture.nativeElement;
  }

  async function setUp(shown: UserResponse = user, signedIn: Account = account) {
    await createComponent(signedIn);
    await show(shown);
  }

  async function show(shown: UserResponse) {
    fixture.componentRef.setInput('user', shown);
    await fixture.whenStable();
  }

  function button(label: string): HTMLButtonElement {
    return Array.from(element.querySelectorAll<HTMLButtonElement>('p-button button')).find(
      (b) => b.textContent?.includes(label) || b.getAttribute('aria-label') === label,
    )!;
  }

  async function click(target: HTMLElement) {
    target.click();
    await fixture.whenStable();
  }

  async function openActions() {
    await click(button('general.button.label.more'));
  }

  function content(item: HTMLElement): HTMLElement {
    return item.querySelector<HTMLElement>('.p-menu-item-content')!;
  }

  function action(label: string): HTMLElement {
    return Array.from(document.querySelectorAll<HTMLElement>('.p-menu-item')).find((item) =>
      item.textContent?.includes(label),
    )!;
  }

  function auditText(): string | undefined {
    return (
      Array.from(element.querySelectorAll('section')).find((section) =>
        section.textContent?.includes(`${DETAIL}.history`),
      )?.textContent ?? undefined
    );
  }

  it('should show the data of the user', async () => {
    await setUp();

    const text = element.textContent;
    expect(element.querySelector('p-avatar')!.textContent?.trim()).toBe('MR');
    expect(element.querySelector('h2')!.textContent).toContain('mario rossi');
    expect(text).toContain('mario.rossi');
    expect(text).toContain('mario@test.it');
    expect(text).toContain('RSSMRA80A01H501U');
    expect(text).toContain('3331234567');
    expect(text).toContain('model.gender.male');
    expect(text).toContain('model.language.it');
    expect(text).toContain('model.user.enabled.true');
    expect(text).toContain('Operators');
    expect(element.querySelector('a[href="mailto:mario@test.it"]')).not.toBeNull();
    expect(element.querySelector('a[href="tel:3331234567"]')).not.toBeNull();
  });

  it('should show the no roles message when the user has no roles', async () => {
    await setUp({ ...user, roles: [] });

    expect(element.textContent).toContain(`${DETAIL}.no-roles`);
  });

  it('should load the audit of the user and show it', async () => {
    await setUp();

    expect(userService.getUserById).toHaveBeenCalledWith(user.id);
    expect(auditText()).toContain('model.audit.created-on');
    expect(auditText()).toContain('Admin');
    expect(auditText()).toContain('model.audit.updated-on');
    expect(auditText()).toContain('Luigi Verdi');
  });

  it('should show the dates of the audit in the current language', async () => {
    registerLocaleData(localeIt, 'it');
    await setUp();

    TestBed.inject(TranslateService).use('it');
    await fixture.whenStable();

    expect(auditText()).toContain('01/01/26');
    expect(auditText()).toContain('02/01/26');
  });

  it('should show the system as author when the audit has no author', async () => {
    userService.getUserById.mockReturnValue(
      of({ ...user, audit: { createdOn: '2026-01-01T10:00:00Z' } }),
    );

    await setUp();

    expect(auditText()).toContain('model.audit.system');
    expect(auditText()).not.toContain('model.audit.updated-on');
  });

  it('should not show the audit while it is loading', async () => {
    userService.getUserById.mockReturnValue(new Subject<UserResponse>());

    await setUp();

    expect(auditText()).toBeUndefined();
    expect(element.textContent).toContain('mario.rossi');
  });

  it('should not show the audit when it cannot be loaded', async () => {
    userService.getUserById.mockReturnValue(throwError(() => new Error('not found')));
    vi.useFakeTimers();
    await createComponent();

    fixture.componentRef.setInput('user', user);
    fixture.detectChanges();
    vi.advanceTimersByTime(MAX_RETRY_ATTEMPTS * RETRY_DELAY_MS);
    fixture.detectChanges();

    expect(auditText()).toBeUndefined();
    expect(element.textContent).toContain('mario.rossi');
  });

  it('should show the audit of the last shown user when an earlier request answers later', async () => {
    const slowMario = new Subject<UserResponse>();
    userService.getUserById.mockImplementation((id: UUID) =>
      id === user.id
        ? slowMario
        : of({ ...luigi, audit: { createdOn: '2026-03-01T10:00:00Z', createdBy: 'Luigi' } }),
    );
    await setUp();

    await show(luigi);
    slowMario.next(loadedUser);
    await fixture.whenStable();

    expect(element.textContent).toContain('luigi.verdi');
    expect(auditText()).not.toContain('Luigi Verdi');
  });

  it('should reload the audit when the same user is shown again with new data', async () => {
    await setUp();

    await show({ ...user, firstname: 'marco' });

    expect(userService.getUserById).toHaveBeenCalledTimes(2);
    expect(element.querySelector('h2')!.textContent).toContain('marco rossi');
  });

  it('should emit the user to edit', async () => {
    await setUp();
    const edited: UserResponse[] = [];
    fixture.componentInstance.edit.subscribe((u) => edited.push(u));

    button(`${DETAIL}.edit`).click();

    expect(edited).toEqual([user]);
  });

  it('should emit the dismiss request when it is closed', async () => {
    await setUp();
    let dismissed = 0;
    fixture.componentInstance.dismiss.subscribe(() => dismissed++);

    button('general.button.label.close').click();

    expect(dismissed).toBe(1);
  });

  it('should ask to reset the password from the actions menu', async () => {
    await setUp();
    await openActions();

    await click(content(action('modules.user-management.reset-password.label')));

    expect(passwordReset.resetPassword).toHaveBeenCalledWith(user.id);
  });

  it('should emit the user to remove from the actions menu', async () => {
    await setUp();
    const removed: UserResponse[] = [];
    fixture.componentInstance.remove.subscribe((u) => removed.push(u));
    await openActions();

    await click(content(action('general.button.label.delete')));

    expect(removed).toEqual([user]);
  });

  it('should not allow to remove the account the user is signed in with', async () => {
    await setUp(user, { ...account, username: user.username });
    const removed: UserResponse[] = [];
    fixture.componentInstance.remove.subscribe((u) => removed.push(u));
    await openActions();

    await click(content(action('general.button.label.delete')));

    expect(action('general.button.label.delete').getAttribute('aria-disabled')).toBe('true');
    expect(removed).toEqual([]);
  });
});
