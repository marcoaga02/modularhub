import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { provideTranslateService, TranslateService } from '@ngx-translate/core';
import { UserFormComponent } from 'app/modules/user-management/pages/user-form/user-form.component';
import { RoleService } from 'app/modules/user-management/api/services/role.service';
import { UserService } from 'app/modules/user-management/api/services/user.service';
import { RoleSummary, UserResponse } from 'app/modules/user-management/api/generated';
import { Gender, Language } from 'app/core/api/generated';
import { UUID } from 'app/core/api/models/uuid';
import { ConfirmDialogService } from 'app/core/utils/services/confirm-dialog.service';
import { PasswordResetService } from 'app/modules/user-management/utils/services/password-reset.service';

describe('UserFormComponent', () => {
  const FORM = 'modules.user-management.pages.user-form';
  const SAVE = `${FORM}.save`;
  const CREATE = `${FORM}.create`;
  const CANCEL = 'general.button.label.cancel';
  const RESET_PASSWORD = 'modules.user-management.reset-password.label';

  const adminRole: RoleSummary = { id: 'admin-role-id' as UUID, name: 'ADMIN', system: true };
  const role: RoleSummary = { id: 'role-id' as UUID, name: 'Operators', system: false };
  const otherRole: RoleSummary = { id: 'other-role-id' as UUID, name: 'Auditors', system: false };
  const user: UserResponse = {
    id: 'user-id' as UUID,
    firstname: 'Mario',
    lastname: 'Rossi',
    gender: Gender.M,
    language: Language.It,
    mobileNumber: '3331234567',
    taxIdNumber: 'RSSMRA80A01H501U',
    email: 'mario@test.it',
    username: 'mario.rossi',
    enabled: true,
    roles: [role],
  };
  const validInput = {
    firstname: 'Luigi',
    lastname: 'Verdi',
    'tax-id-number': 'VRDLGU85B02F205X',
    'mobile-number': '3339876543',
    email: 'luigi@test.it',
    username: 'luigi.verdi',
  };
  const PASSWORD = 'Secret123!';

  let fixture: ComponentFixture<UserFormComponent>;
  let element: HTMLElement;
  let ref: DynamicDialogRef;
  let closedWith: unknown[];
  let userService: {
    getUserById: ReturnType<typeof vi.fn>;
    createUser: ReturnType<typeof vi.fn>;
    updateUser: ReturnType<typeof vi.fn>;
  };
  let confirmDialog: { confirm: ReturnType<typeof vi.fn> };
  let passwordReset: { resetPassword: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    ref = new DynamicDialogRef();
    closedWith = [];
    ref.onClose.subscribe((result) => closedWith.push(result));
    userService = {
      getUserById: vi.fn().mockReturnValue(of(user)),
      createUser: vi.fn().mockReturnValue(of(user)),
      updateUser: vi.fn().mockReturnValue(of(user)),
    };
    confirmDialog = { confirm: vi.fn() };
    passwordReset = { resetPassword: vi.fn() };
  });

  async function setUp(userId?: UUID) {
    await TestBed.configureTestingModule({
      imports: [UserFormComponent],
      providers: [
        provideTranslateService(),
        { provide: ConfirmDialogService, useValue: confirmDialog },
        { provide: DynamicDialogConfig, useValue: { data: { userId } } },
        { provide: DynamicDialogRef, useValue: ref },
        { provide: RoleService, useValue: { getRoles: () => of([adminRole, role, otherRole]) } },
        { provide: UserService, useValue: userService },
        { provide: PasswordResetService, useValue: passwordReset },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UserFormComponent);
    element = fixture.nativeElement;
    await fixture.whenStable();
  }

  function input(id: string): HTMLInputElement {
    return element.querySelector<HTMLInputElement>(`input#${id}`)!;
  }

  async function type(target: HTMLInputElement, value: string) {
    target.value = value;
    target.dispatchEvent(new Event('input'));
    target.dispatchEvent(new Event('blur'));
    await fixture.whenStable();
  }

  async function chooseGender(label: string) {
    Array.from(element.querySelectorAll<HTMLElement>('#gender p-togglebutton'))
      .find((option) => option.textContent?.includes(label))!
      .click();
    await fixture.whenStable();
  }

  async function fillValidForm() {
    for (const [id, value] of Object.entries(validInput)) {
      await type(input(id), value);
    }
    await type(input('password'), PASSWORD);
    await type(input('password-confirmation'), PASSWORD);
    await chooseGender('model.gender.female');
  }

  function button(label: string): HTMLButtonElement {
    return Array.from(element.querySelectorAll<HTMLButtonElement>('p-button button')).find((b) =>
      b.textContent?.includes(label),
    )!;
  }

  async function click(target: HTMLElement) {
    target.click();
    await fixture.whenStable();
  }

  function closeButton(): HTMLButtonElement {
    return element.querySelector<HTMLButtonElement>(
      'header p-button button[aria-label="general.button.label.close"]',
    )!;
  }

  function enabledSwitch(): HTMLInputElement {
    return input('enabled');
  }

  function errors(): string[] {
    return Array.from(element.querySelectorAll('p-message')).map((m) => m.textContent!.trim());
  }

  function roleOption(name: string): HTMLElement {
    return Array.from(element.querySelectorAll<HTMLElement>('#roles li[role="option"]')).find(
      (option) => option.textContent?.includes(name),
    )!;
  }

  async function toggleRole(name: string) {
    await click(roleOption(name));
  }

  function selectedRoleNames(): string[] {
    return Array.from(
      element.querySelectorAll<HTMLElement>('#roles li[role="option"][aria-selected="true"]'),
    ).map((option) => option.textContent!.trim());
  }

  function acceptConfirmation() {
    confirmDialog.confirm.mock.lastCall![2]();
  }

  describe('create', () => {
    it('should show the creation title, the password fields and a disabled user', async () => {
      await setUp();

      expect(userService.getUserById).not.toHaveBeenCalled();
      expect(element.querySelector('h2')!.textContent).toContain(`${FORM}.creation-title`);
      expect(element.querySelector('p-avatar')).toBeNull();
      expect(input('password').disabled).toBe(false);
      expect(input('password-confirmation').disabled).toBe(false);
      expect(element.textContent).not.toContain(RESET_PASSWORD);
      expect(enabledSwitch().checked).toBe(false);
      expect(element.textContent).toContain(`${FORM}.enabled-hint.false`);
      expect(button(CREATE)).toBeDefined();
    });

    it('should show no errors before the fields are touched', async () => {
      await setUp();

      expect(errors()).toEqual([]);
    });

    it('should show the error of a field when it is left invalid', async () => {
      await setUp();

      await type(input('tax-id-number'), 'ABC');
      await type(input('mobile-number'), '333-123');
      await type(input('email'), 'not-an-email');
      await type(input('firstname'), '');

      expect(errors()).toEqual([
        `${FORM}.errors.required`,
        `${FORM}.errors.tax-id-number`,
        `${FORM}.errors.mobile-number`,
        `${FORM}.errors.email`,
      ]);
      expect(input('firstname').classList).toContain('p-invalid');
    });

    it('should show an error when the password confirmation does not match', async () => {
      await setUp();

      await type(input('password'), PASSWORD);
      await type(input('password-confirmation'), 'Other123!');

      expect(errors()).toEqual([`${FORM}.errors.password-mismatch`]);
    });

    it('should mark all the invalid fields and not save when the form is invalid', async () => {
      await setUp();

      await click(button(CREATE));

      expect(input('firstname').classList).toContain('p-invalid');
      expect(errors()).toHaveLength(9);
      expect(userService.createUser).not.toHaveBeenCalled();
      expect(closedWith).toEqual([]);
    });

    it('should not save when the passwords do not match', async () => {
      await setUp();
      await fillValidForm();
      await type(input('password-confirmation'), 'Other123!');

      await click(button(CREATE));

      expect(userService.createUser).not.toHaveBeenCalled();
    });

    it('should create the user with the password and close with the created user', async () => {
      await setUp();
      await fillValidForm();
      await toggleRole('Auditors');
      await click(enabledSwitch());

      await click(button(CREATE));

      expect(userService.createUser).toHaveBeenCalledWith({
        firstname: 'Luigi',
        lastname: 'Verdi',
        gender: Gender.F,
        language: Language.En,
        mobileNumber: '3339876543',
        taxIdNumber: 'VRDLGU85B02F205X',
        email: 'luigi@test.it',
        username: 'luigi.verdi',
        roleIds: [otherRole.id],
        enabled: true,
        password: PASSWORD,
      });
      expect(closedWith).toEqual([user]);
    });

    it('should stay open and allow to save again when the creation fails', async () => {
      userService.createUser.mockReturnValue(throwError(() => new Error('conflict')));
      await setUp();
      await fillValidForm();

      await click(button(CREATE));
      await click(button(CREATE));

      expect(userService.createUser).toHaveBeenCalledTimes(2);
      expect(closedWith).toEqual([]);
    });
  });

  describe('edit', () => {
    it('should show the form only when the user is loaded', async () => {
      const loadedUser = new Subject<UserResponse>();
      userService.getUserById.mockReturnValue(loadedUser);
      await setUp(user.id);

      expect(element.querySelector('input#firstname')).toBeNull();
      expect(button(SAVE).disabled).toBe(true);

      loadedUser.next(user);
      await fixture.whenStable();

      expect(input('firstname').value).toBe('Mario');
      expect(button(SAVE).disabled).toBe(false);
    });

    it('should show the user in the header and fill the form', async () => {
      await setUp(user.id);

      expect(userService.getUserById).toHaveBeenCalledWith(user.id);
      expect(element.querySelector('h2')!.textContent).toContain(`${FORM}.edit-title`);
      expect(element.querySelector('header p-avatar')!.textContent).toContain('MR');
      expect(element.querySelector('header')!.textContent).toContain('Mario Rossi · mario.rossi');
      expect(input('firstname').value).toBe('Mario');
      expect(input('lastname').value).toBe('Rossi');
      expect(input('email').value).toBe('mario@test.it');
      expect(input('username').value).toBe('mario.rossi');
      expect(enabledSwitch().checked).toBe(true);
      expect(element.textContent).toContain(`${FORM}.enabled-hint.true`);
    });

    it('should replace the password fields with the password reset', async () => {
      await setUp(user.id);

      expect(input('password')).toBeNull();
      expect(input('password-confirmation')).toBeNull();
      expect(element.textContent).toContain(`${FORM}.reset-password.description`);
    });

    it('should update the user without the password and close with the updated user', async () => {
      await setUp(user.id);
      await type(input('firstname'), 'Marco');
      await click(enabledSwitch());

      await click(button(SAVE));

      expect(userService.updateUser).toHaveBeenCalledWith(user.id, {
        firstname: 'Marco',
        lastname: 'Rossi',
        gender: Gender.M,
        language: Language.It,
        mobileNumber: '3331234567',
        taxIdNumber: 'RSSMRA80A01H501U',
        email: 'mario@test.it',
        username: 'mario.rossi',
        roleIds: [role.id],
        enabled: false,
      });
      expect(userService.createUser).not.toHaveBeenCalled();
      expect(closedWith).toEqual([user]);
    });

    it('should ask to reset the password of the edited user', async () => {
      await setUp(user.id);

      await click(button(RESET_PASSWORD));

      expect(passwordReset.resetPassword).toHaveBeenCalledWith(user.id);
    });
  });

  describe('cancel', () => {
    it('should close without confirmation when nothing is changed in creation', async () => {
      await setUp();

      await click(button(CANCEL));

      expect(confirmDialog.confirm).not.toHaveBeenCalled();
      expect(closedWith).toEqual([undefined]);
    });

    it('should close without confirmation when nothing is changed in edit', async () => {
      await setUp(user.id);

      await click(closeButton());

      expect(confirmDialog.confirm).not.toHaveBeenCalled();
      expect(closedWith).toEqual([undefined]);
    });

    it('should close without confirmation when a changed field is restored', async () => {
      await setUp(user.id);
      await type(input('firstname'), 'Marco');
      await type(input('firstname'), 'Mario');

      await click(button(CANCEL));

      expect(confirmDialog.confirm).not.toHaveBeenCalled();
      expect(closedWith).toEqual([undefined]);
    });

    it('should ask a confirmation when the roles are changed', async () => {
      await setUp(user.id);
      await toggleRole('Auditors');

      await click(closeButton());

      expect(confirmDialog.confirm).toHaveBeenCalledOnce();
      expect(closedWith).toEqual([]);
    });

    it('should not ask a confirmation when the same roles are selected in another order', async () => {
      userService.getUserById.mockReturnValue(of({ ...user, roles: [role, otherRole] }));
      await setUp(user.id);
      await toggleRole('Operators');
      await toggleRole('Operators');

      await click(button(CANCEL));

      expect(confirmDialog.confirm).not.toHaveBeenCalled();
    });

    it('should close without result only after the confirmation is accepted when a field is changed', async () => {
      await setUp();
      await type(input('firstname'), 'Luigi');

      await click(button(CANCEL));
      expect(confirmDialog.confirm).toHaveBeenCalledWith(
        `${FORM}.confirmation-cancel-dialog.title`,
        `${FORM}.confirmation-cancel-dialog.message`,
        expect.any(Function),
      );
      expect(closedWith).toEqual([]);

      acceptConfirmation();

      expect(closedWith).toEqual([undefined]);
    });
  });

  describe('roles', () => {
    it('should list all the roles and mark the system ones', async () => {
      await setUp();

      const options = Array.from(element.querySelectorAll<HTMLElement>('#roles li[role="option"]'));
      expect(options.map((option) => option.textContent)).toEqual([
        expect.stringContaining('ADMIN'),
        expect.stringContaining('Operators'),
        expect.stringContaining('Auditors'),
      ]);
      expect(options.map((option) => option.querySelector('p-tag')?.textContent?.trim())).toEqual([
        `${FORM}.roles.system`,
        undefined,
        undefined,
      ]);
    });

    it('should select the roles of the edited user', async () => {
      await setUp(user.id);

      expect(selectedRoleNames()).toEqual(['Operators']);
    });

    it('should count the selected roles', async () => {
      await setUp(user.id);
      const translateService = TestBed.inject(TranslateService);
      translateService.setTranslation('en', {
        modules: {
          'user-management': {
            pages: { 'user-form': { roles: { 'selected-count': '{{selected}} of {{total}}' } } },
          },
        },
      });
      translateService.use('en');
      await fixture.whenStable();
      expect(element.textContent).toContain('1 of 3');

      await toggleRole('ADMIN');

      expect(element.textContent).toContain('2 of 3');
    });

    it('should save the roles toggled in the list', async () => {
      await setUp(user.id);

      await toggleRole('Operators');
      await toggleRole('ADMIN');
      await click(button(SAVE));

      expect(userService.updateUser).toHaveBeenCalledWith(
        user.id,
        expect.objectContaining({ roleIds: [adminRole.id] }),
      );
    });
  });
});
