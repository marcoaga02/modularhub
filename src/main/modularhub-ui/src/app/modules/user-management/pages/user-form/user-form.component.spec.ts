import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { DialogService, DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { MessageService, ToastMessageOptions } from 'primeng/api';
import { provideTranslateService } from '@ngx-translate/core';
import { UserFormComponent } from 'app/modules/user-management/pages/user-form/user-form.component';
import { RoleService } from 'app/modules/user-management/api/services/role.service';
import { UserService } from 'app/modules/user-management/api/services/user.service';
import { RoleSummary, UserResponse } from 'app/modules/user-management/api/generated';
import { Gender, Language } from 'app/core/api/generated';
import { UUID } from 'app/core/api/models/uuid';
import { ConfirmDialogService } from 'app/core/utils/services/confirm-dialog.service';

describe('UserFormComponent', () => {
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

  let fixture: ComponentFixture<UserFormComponent>;
  let element: HTMLElement;
  let buttonClick: Subject<'confirm' | 'cancel' | 'roles'>;
  let formValid: BehaviorSubject<boolean>;
  let ref: DynamicDialogRef;
  let closedWith: unknown[];
  let userService: {
    getUserById: ReturnType<typeof vi.fn>;
    createUser: ReturnType<typeof vi.fn>;
    updateUser: ReturnType<typeof vi.fn>;
    resetPassword: ReturnType<typeof vi.fn>;
  };
  let dialogService: { open: ReturnType<typeof vi.fn> };
  let rolesDialogClose: Subject<UUID[] | undefined>;
  let confirmDialog: { confirm: ReturnType<typeof vi.fn> };
  let messages: ToastMessageOptions[];

  beforeEach(() => {
    buttonClick = new Subject();
    formValid = new BehaviorSubject(false);
    ref = new DynamicDialogRef();
    closedWith = [];
    ref.onClose.subscribe((result) => closedWith.push(result));
    userService = {
      getUserById: vi.fn().mockReturnValue(of(user)),
      createUser: vi.fn().mockReturnValue(of(user)),
      updateUser: vi.fn().mockReturnValue(of(user)),
      resetPassword: vi.fn().mockReturnValue(of(undefined)),
    };
    dialogService = {
      open: vi.fn(() => {
        rolesDialogClose = new Subject();
        return { onClose: rolesDialogClose.asObservable() };
      }),
    };
    confirmDialog = { confirm: vi.fn() };
  });

  async function setUp(userId?: UUID) {
    await TestBed.configureTestingModule({
      imports: [UserFormComponent],
      providers: [
        provideTranslateService(),
        MessageService,
        { provide: ConfirmDialogService, useValue: confirmDialog },
        { provide: DynamicDialogConfig, useValue: { data: { buttonClick, formValid, userId } } },
        { provide: DynamicDialogRef, useValue: ref },
        { provide: DialogService, useValue: dialogService },
        { provide: RoleService, useValue: { getRoles: () => of([role, otherRole]) } },
        { provide: UserService, useValue: userService },
      ],
    }).compileComponents();

    messages = [];
    TestBed.inject(MessageService).messageObserver.subscribe((m) =>
      messages.push(...(Array.isArray(m) ? m : [m])),
    );

    fixture = TestBed.createComponent(UserFormComponent);
    element = fixture.nativeElement;
    await fixture.whenStable();
  }

  function input(id: string): HTMLInputElement {
    return element.querySelector<HTMLInputElement>(`input#${id}`)!;
  }

  function passwordInput(): HTMLInputElement {
    return element.querySelector<HTMLInputElement>('p-password input')!;
  }

  async function type(target: HTMLInputElement, value: string) {
    target.value = value;
    target.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  async function fillValidForm() {
    for (const [id, value] of Object.entries(validInput)) {
      await type(input(id), value);
    }
    await type(passwordInput(), 'Secret123!');
    element.querySelector<HTMLInputElement>('#gender-female input')!.click();
    await fixture.whenStable();
  }

  async function press(action: 'confirm' | 'cancel' | 'roles') {
    buttonClick.next(action);
    await fixture.whenStable();
  }

  async function closeRolesDialogWith(roleIds: UUID[] | undefined) {
    await press('roles');
    rolesDialogClose.next(roleIds);
  }

  function acceptConfirmation() {
    confirmDialog.confirm.mock.lastCall![2]();
  }

  function roleIdsPassedToRolesDialog(): UUID[] {
    const config = dialogService.open.mock.lastCall![1];
    return config.data.selectedRoleIds;
  }

  describe('create', () => {
    it('should not load any user and require the password', async () => {
      await setUp();

      expect(userService.getUserById).not.toHaveBeenCalled();
      expect(passwordInput().disabled).toBe(false);
      expect(element.textContent).not.toContain(
        'modules.user-management.pages.user-form.reset-password.label',
      );
    });

    it('should publish the validity of the form while it is filled', async () => {
      await setUp();

      await fillValidForm();
      expect(formValid.value).toBe(true);

      await type(input('email'), 'not-an-email');
      expect(formValid.value).toBe(false);
    });

    it('should mark the invalid fields and not save when the form is invalid', async () => {
      await setUp();

      await press('confirm');

      expect(input('firstname').classList).toContain('p-invalid');
      expect(userService.createUser).not.toHaveBeenCalled();
      expect(closedWith).toEqual([]);
    });

    it('should create the user with the password and close with the created user', async () => {
      await setUp();
      await fillValidForm();
      await closeRolesDialogWith([otherRole.id]);

      await press('confirm');

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
        enabled: false,
        password: 'Secret123!',
      });
      expect(closedWith).toEqual([user]);
    });
  });

  describe('edit', () => {
    it('should show the form only when the user is loaded', async () => {
      const loadedUser = new Subject<UserResponse>();
      userService.getUserById.mockReturnValue(loadedUser);
      await setUp(user.id);

      expect(element.querySelector('input#firstname')).toBeNull();

      loadedUser.next(user);
      await fixture.whenStable();

      expect(input('firstname').value).toBe('Mario');
    });

    it('should fill the form with the user and disable the password', async () => {
      await setUp(user.id);

      expect(userService.getUserById).toHaveBeenCalledWith(user.id);
      expect(input('firstname').value).toBe('Mario');
      expect(input('lastname').value).toBe('Rossi');
      expect(input('email').value).toBe('mario@test.it');
      expect(input('username').value).toBe('mario.rossi');
      expect(passwordInput().disabled).toBe(true);
      expect(formValid.value).toBe(true);
    });

    it('should update the user without the password and close with the updated user', async () => {
      await setUp(user.id);
      await type(input('firstname'), 'Marco');

      await press('confirm');

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
        enabled: true,
      });
      expect(userService.createUser).not.toHaveBeenCalled();
      expect(closedWith).toEqual([user]);
    });

    it('should reset the password after confirmation and show a message', async () => {
      await setUp(user.id);
      const resetButton = Array.from(
        element.querySelectorAll<HTMLButtonElement>('p-button button'),
      ).find((b) =>
        b.textContent?.includes('modules.user-management.pages.user-form.reset-password.label'),
      )!;

      resetButton.click();
      await fixture.whenStable();
      expect(confirmDialog.confirm).toHaveBeenCalledWith(
        'modules.user-management.pages.user-form.reset-password.confirmation-title',
        'modules.user-management.pages.user-form.reset-password.confirmation-message',
        expect.any(Function),
      );
      expect(userService.resetPassword).not.toHaveBeenCalled();

      acceptConfirmation();

      expect(userService.resetPassword).toHaveBeenCalledWith(user.id);
      expect(messages).toEqual([
        {
          severity: 'success',
          summary: 'modules.user-management.pages.user-form.reset-password.success',
        },
      ]);
    });
  });

  describe('cancel', () => {
    it('should close without result only after the confirmation is accepted', async () => {
      await setUp();

      await press('cancel');
      expect(confirmDialog.confirm).toHaveBeenCalledWith(
        'modules.user-management.pages.user-form.confirmation-cancel-dialog.title',
        'modules.user-management.pages.user-form.confirmation-cancel-dialog.message',
        expect.any(Function),
      );
      expect(closedWith).toEqual([]);

      acceptConfirmation();

      expect(closedWith).toEqual([undefined]);
    });
  });

  describe('roles', () => {
    it('should open the roles dialog with all the roles and the roles of the user', async () => {
      await setUp(user.id);

      await press('roles');

      const config = dialogService.open.mock.lastCall![1];
      expect(config.data.roles).toEqual([role, otherRole]);
      expect(config.data.selectedRoleIds).toEqual([role.id]);
    });

    it('should keep the selected roles when the roles dialog confirms them', async () => {
      await setUp();

      await closeRolesDialogWith([role.id]);
      await press('roles');

      expect(roleIdsPassedToRolesDialog()).toEqual([role.id]);
    });

    it('should clear the roles when the roles dialog confirms an empty selection', async () => {
      await setUp();
      await closeRolesDialogWith([role.id]);

      await closeRolesDialogWith([]);
      await press('roles');

      expect(roleIdsPassedToRolesDialog()).toEqual([]);
    });

    it('should keep the previous roles when the roles dialog is closed without confirming', async () => {
      await setUp();
      await closeRolesDialogWith([role.id]);

      await closeRolesDialogWith(undefined);
      await press('roles');

      expect(roleIdsPassedToRolesDialog()).toEqual([role.id]);
    });
  });
});
