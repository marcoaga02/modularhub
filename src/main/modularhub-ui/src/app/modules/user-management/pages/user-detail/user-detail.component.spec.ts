import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { provideTranslateService, TranslateService } from '@ngx-translate/core';
import { registerLocaleData } from '@angular/common';
import localeIt from '@angular/common/locales/it';
import { UserDetailComponent } from 'app/modules/user-management/pages/user-detail/user-detail.component';
import { UserService } from 'app/modules/user-management/api/services/user.service';
import { RoleSummary, UserResponse } from 'app/modules/user-management/api/generated';
import { Gender, Language } from 'app/core/api/generated';
import { UUID } from 'app/core/api/models/uuid';
import { MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS } from 'app/core/utils/constants/retry';

describe('UserDetailComponent', () => {
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
    audit: {
      createdOn: '2026-01-01T10:00:00Z',
      createdBy: 'Admin',
      updatedOn: '2026-01-02T10:00:00Z',
      updatedBy: 'Luigi Verdi',
    },
  };

  let fixture: ComponentFixture<UserDetailComponent>;
  let element: HTMLElement;
  let userService: { getUserById: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    userService = { getUserById: vi.fn().mockReturnValue(of(user)) };

    await TestBed.configureTestingModule({
      imports: [UserDetailComponent],
      providers: [
        provideTranslateService({ lang: 'en' }),
        { provide: UserService, useValue: userService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UserDetailComponent);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  async function showUser(id: UUID | undefined) {
    fixture.componentRef.setInput('userId', id);
    await fixture.whenStable();
  }

  it('should show nothing and load nothing without a user id', () => {
    expect(userService.getUserById).not.toHaveBeenCalled();
    expect(element.textContent?.trim()).toBe('');
  });

  it('should load and show the user of the given id', async () => {
    await showUser(user.id);

    expect(userService.getUserById).toHaveBeenCalledWith(user.id);
    const text = element.textContent;
    expect(element.querySelector('p-avatar')!.textContent?.trim()).toBe('MR');
    expect(text).toContain('mario rossi');
    expect(text).toContain('mario.rossi');
    expect(text).toContain('mario@test.it');
    expect(text).toContain('RSSMRA80A01H501U');
    expect(text).toContain('Operators');
    expect(text).toContain('Luigi Verdi');
  });

  it('should show the dates in the new language when the language changes', async () => {
    registerLocaleData(localeIt, 'it');
    await showUser(user.id);
    expect(element.textContent).toContain('January 2026');

    TestBed.inject(TranslateService).use('it');
    await fixture.whenStable();

    expect(element.textContent).toContain('gennaio 2026');
  });

  it('should show the spinner while the user is loading', async () => {
    userService.getUserById.mockReturnValue(new Subject<UserResponse>());

    await showUser(user.id);

    expect(element.querySelector('p-progress-spinner')).not.toBeNull();
  });

  it('should show the no roles message when the user has no roles', async () => {
    userService.getUserById.mockReturnValue(of({ ...user, roles: [] }));

    await showUser(user.id);

    expect(element.textContent).toContain('modules.user-management.pages.user-detail.no-roles');
  });

  it('should show nothing when the user cannot be loaded', async () => {
    userService.getUserById.mockReturnValue(throwError(() => new Error('not found')));
    vi.useFakeTimers();

    fixture.componentRef.setInput('userId', user.id);
    fixture.detectChanges();
    vi.advanceTimersByTime(MAX_RETRY_ATTEMPTS * RETRY_DELAY_MS);
    fixture.detectChanges();

    expect(element.querySelector('p-progress-spinner')).toBeNull();
    expect(element.textContent?.trim()).toBe('');
  });

  it('should show the last selected user when an earlier request answers later', async () => {
    const luigi: UserResponse = { ...user, id: 'luigi-id' as UUID, username: 'luigi.verdi' };
    const slowMario = new Subject<UserResponse>();
    userService.getUserById.mockImplementation((id: UUID) =>
      id === user.id ? slowMario : of(luigi),
    );

    await showUser(user.id);
    await showUser(luigi.id);
    slowMario.next(user);
    await fixture.whenStable();

    expect(element.textContent).toContain('luigi.verdi');
    expect(element.textContent).not.toContain('mario.rossi');
  });

  it('should clear the user when the id is removed', async () => {
    await showUser(user.id);

    await showUser(undefined);

    expect(element.textContent?.trim()).toBe('');
  });
});
