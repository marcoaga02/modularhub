import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { signal, Signal } from '@angular/core';
import { of, Subject } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { MessageService, ToastMessageOptions } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { UserListComponent } from 'app/modules/user-management/pages/user-list/user-list.component';
import { UserFormComponent } from 'app/modules/user-management/pages/user-form/user-form.component';
import { UserService } from 'app/modules/user-management/api/services/user.service';
import { UserCriteria } from 'app/modules/user-management/api/user-criteria';
import { UserResponse } from 'app/modules/user-management/api/generated';
import { AccountService } from 'app/core/api/services/account.service';
import { Account, Gender, Language } from 'app/core/api/generated';
import { HttpHeader } from 'app/core/api/http-header';
import { UUID } from 'app/core/api/models/uuid';
import { ConfirmDialogService } from 'app/core/utils/services/confirm-dialog.service';

describe('UserListComponent', () => {
  const FILTER_DEBOUNCE_MS = 250;

  const mario = userWith('mario-id', 'mario');
  const luigi = userWith('luigi-id', 'luigi');
  const account: Account = {
    identityId: 'identity-id',
    email: 'mario@test.it',
    username: 'mario',
    firstName: 'Mario',
    lastName: 'Rossi',
    permissions: ['USER_MANAGEMENT'],
    preferences: { language: Language.It },
  };

  function userWith(id: string, username: string): UserResponse {
    return {
      id: id as UUID,
      firstname: username,
      lastname: 'Rossi',
      gender: Gender.M,
      taxIdNumber: 'RSSMRA80A01H501U',
      email: `${username}@test.it`,
      username,
      enabled: true,
      roles: [],
      audit: { createdOn: '2026-01-01T10:00:00Z', updatedOn: '2026-01-02T10:00:00Z' },
    };
  }

  function page(users: UserResponse[] | null, total = users?.length ?? 0) {
    return of(
      new HttpResponse({
        body: users,
        headers: new HttpHeaders({ [HttpHeader.TotalCount]: String(total) }),
      }),
    );
  }

  let fixture: ComponentFixture<UserListComponent>;
  let element: HTMLElement;
  let userService: {
    getUsers: ReturnType<typeof vi.fn>;
    getUserById: ReturnType<typeof vi.fn>;
    deleteUsers: ReturnType<typeof vi.fn>;
  };
  let accountService: {
    account: Signal<Account | null>;
    reload: ReturnType<typeof vi.fn>;
  };
  let dialogService: { open: ReturnType<typeof vi.fn> };
  let formDialogClose: Subject<UserResponse | undefined>;
  let confirmDialog: { confirm: ReturnType<typeof vi.fn> };
  let messages: ToastMessageOptions[];

  async function setUp(users: UserResponse[] | null = [mario, luigi], total?: number) {
    userService = {
      getUsers: vi.fn().mockReturnValue(page(users, total)),
      getUserById: vi.fn().mockReturnValue(of(mario)),
      deleteUsers: vi.fn().mockReturnValue(of([undefined])),
    };
    accountService = {
      account: signal(account).asReadonly(),
      reload: vi.fn().mockResolvedValue(undefined),
    };
    dialogService = {
      open: vi.fn(() => {
        formDialogClose = new Subject();
        return { onClose: formDialogClose.asObservable() };
      }),
    };
    confirmDialog = { confirm: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [UserListComponent],
      providers: [
        provideTranslateService({ lang: 'en' }),
        MessageService,
        { provide: ConfirmDialogService, useValue: confirmDialog },
        { provide: UserService, useValue: userService },
        { provide: AccountService, useValue: accountService },
        { provide: DialogService, useValue: dialogService },
      ],
    }).compileComponents();

    messages = [];
    TestBed.inject(MessageService).messageObserver.subscribe((m) =>
      messages.push(...(Array.isArray(m) ? m : [m])),
    );

    fixture = TestBed.createComponent(UserListComponent);
    element = fixture.nativeElement;
    await fixture.whenStable();
  }

  function rows(): HTMLTableRowElement[] {
    return Array.from(element.querySelectorAll<HTMLTableRowElement>('tbody tr'));
  }

  async function selectRow(index: number) {
    rows()[index].querySelector<HTMLInputElement>('p-tablecheckbox input')!.click();
    await fixture.whenStable();
  }

  function button(label: string): HTMLButtonElement {
    return Array.from(element.querySelectorAll<HTMLButtonElement>('p-button button')).find((b) =>
      b.textContent?.includes(label.toUpperCase()),
    )!;
  }

  async function click(label: string) {
    button(label).click();
    await fixture.whenStable();
  }

  function lastCriteria(): UserCriteria {
    return userService.getUsers.mock.lastCall![0];
  }

  describe('loading', () => {
    it('should load the first page and show the users', async () => {
      await setUp();

      expect(lastCriteria().page).toBe(0);
      expect(lastCriteria().size).toBe(20);
      expect(rows()).toHaveLength(2);
      expect(rows()[0].textContent).toContain('mario@test.it');
      expect(element.querySelector('.p-datatable-mask')).toBeNull();
    });

    it('should stop loading and show the empty message when the response has no body', async () => {
      await setUp(null);

      expect(element.querySelector('.p-datatable-mask')).toBeNull();
      expect(element.textContent).toContain(
        'modules.user-management.pages.user-list.empty-message',
      );
    });

    it('should reload the users filtered by the typed text', async () => {
      await setUp();
      const search = element.querySelector<HTMLInputElement>('p-iconfield input')!;

      search.value = 'mar';
      search.dispatchEvent(new Event('input'));
      await new Promise((resolve) => setTimeout(resolve, FILTER_DEBOUNCE_MS));

      expect(lastCriteria().filter.text).toBe('mar');
    });

    it('should load the page and the page size requested by the paginator', async () => {
      await setUp([mario, luigi], 60);

      element.querySelector<HTMLButtonElement>('.p-paginator-next')!.click();
      await fixture.whenStable();

      expect(lastCriteria().page).toBe(1);
      expect(lastCriteria().size).toBe(20);
    });
  });

  describe('buttons', () => {
    it('should disable edit and delete when no user is selected', async () => {
      await setUp();

      expect(button('general.button.label.edit').disabled).toBe(true);
      expect(button('general.button.label.delete').disabled).toBe(true);
    });

    it('should enable edit and delete when another user is selected', async () => {
      await setUp();

      await selectRow(1);

      expect(button('general.button.label.edit').disabled).toBe(false);
      expect(button('general.button.label.delete').disabled).toBe(false);
    });

    it('should disable edit when more users are selected', async () => {
      await setUp();

      await selectRow(0);
      await selectRow(1);

      expect(button('general.button.label.edit').disabled).toBe(true);
    });

    it('should disable delete when the current account is selected', async () => {
      await setUp();

      await selectRow(0);

      expect(button('general.button.label.delete').disabled).toBe(true);
    });
  });

  describe('add', () => {
    it('should open the user form without a user', async () => {
      await setUp();

      await click('general.button.label.add');

      const [component, config] = dialogService.open.mock.lastCall!;
      expect(component).toBe(UserFormComponent);
      expect(config.header).toBe('modules.user-management.pages.user-form.creation-title');
      expect(config.data.userId).toBeUndefined();
    });

    it('should show a message and reload the first page when a user is created', async () => {
      await setUp();
      await click('general.button.label.add');
      const loadsBefore = userService.getUsers.mock.calls.length;

      formDialogClose.next(luigi);
      await fixture.whenStable();

      expect(messages).toEqual([
        { severity: 'success', detail: 'modules.user-management.pages.user-list.creation-message' },
      ]);
      expect(userService.getUsers.mock.calls.length).toBe(loadsBefore + 1);
      expect(lastCriteria().offset).toBe(0);
      expect(accountService.reload).not.toHaveBeenCalled();
    });

    it('should do nothing when the user form is closed without result', async () => {
      await setUp();
      await click('general.button.label.add');
      const loadsBefore = userService.getUsers.mock.calls.length;

      formDialogClose.next(undefined);

      expect(messages).toEqual([]);
      expect(userService.getUsers.mock.calls.length).toBe(loadsBefore);
    });
  });

  describe('edit', () => {
    it('should open the user form with the selected user', async () => {
      await setUp();
      await selectRow(1);

      await click('general.button.label.edit');

      const config = dialogService.open.mock.lastCall![1];
      expect(config.header).toBe('modules.user-management.pages.user-form.edit-title');
      expect(config.data.userId).toBe(luigi.id);
    });

    it('should reload the current account when the current account is edited', async () => {
      await setUp();
      await selectRow(0);
      await click('general.button.label.edit');

      formDialogClose.next(mario);
      await fixture.whenStable();

      expect(accountService.reload).toHaveBeenCalledOnce();
      expect(messages).toEqual([
        { severity: 'success', detail: 'modules.user-management.pages.user-list.edit-message' },
      ]);
    });

    it('should not reload the current account when another user is edited', async () => {
      await setUp();
      await selectRow(1);
      await click('general.button.label.edit');

      formDialogClose.next(luigi);
      await fixture.whenStable();

      expect(accountService.reload).not.toHaveBeenCalled();
    });
  });

  describe('delete', () => {
    it('should delete the selected users after confirmation, show a message and reload', async () => {
      await setUp();
      await selectRow(1);
      await click('general.button.label.delete');
      const loadsBefore = userService.getUsers.mock.calls.length;
      expect(confirmDialog.confirm).toHaveBeenCalledWith(
        'modules.user-management.pages.user-list.confirmation-delete-dialog.title',
        'modules.user-management.pages.user-list.confirmation-delete-dialog.message',
        expect.any(Function),
      );
      expect(userService.deleteUsers).not.toHaveBeenCalled();

      confirmDialog.confirm.mock.lastCall![2]();
      await fixture.whenStable();

      expect(userService.deleteUsers).toHaveBeenCalledWith([luigi]);
      expect(messages).toEqual([
        { severity: 'success', summary: 'modules.user-management.pages.user-list.delete-message' },
      ]);
      expect(userService.getUsers.mock.calls.length).toBe(loadsBefore + 1);
      expect(button('general.button.label.delete').disabled).toBe(true);
    });
  });
});
