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
    deleteUser: ReturnType<typeof vi.fn>;
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
      deleteUser: vi.fn().mockReturnValue(of(undefined)),
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

  const LIST = 'modules.user-management.pages.user-list';

  function rows(): HTMLTableRowElement[] {
    return Array.from(element.querySelectorAll<HTMLTableRowElement>('tbody tr'));
  }

  async function selectRow(index: number) {
    rows()[index].click();
    await fixture.whenStable();
  }

  function detail(): HTMLElement | null {
    return element.querySelector('app-user-detail');
  }

  function button(label: string, container: HTMLElement = element): HTMLButtonElement {
    return Array.from(container.querySelectorAll<HTMLButtonElement>('p-button button')).find(
      (b) => b.textContent?.includes(label) || b.getAttribute('aria-label') === label,
    )!;
  }

  async function click(target: HTMLElement) {
    target.click();
    await fixture.whenStable();
  }

  function content(item: HTMLElement): HTMLElement {
    return item.querySelector<HTMLElement>('.p-menu-item-content')!;
  }

  function menuItem(label: string): HTMLElement {
    return Array.from(document.querySelectorAll<HTMLElement>('.p-menu-item')).find((item) =>
      item.textContent?.includes(label),
    )!;
  }

  async function search(text: string) {
    const input = element.querySelector<HTMLInputElement>('p-iconfield input')!;
    input.value = text;
    input.dispatchEvent(new Event('input'));
    await new Promise((resolve) => setTimeout(resolve, FILTER_DEBOUNCE_MS));
    await fixture.whenStable();
  }

  function lastCriteria(): UserCriteria {
    return userService.getUsers.mock.lastCall![0];
  }

  describe('loading', () => {
    it('should load the first page and show the users with their total', async () => {
      await setUp([mario, luigi], 2);

      expect(lastCriteria().page).toBe(0);
      expect(lastCriteria().size).toBe(20);
      expect(rows()).toHaveLength(2);
      expect(rows()[0].textContent).toContain('mario Rossi');
      expect(rows()[0].textContent).toContain('mario@test.it');
      expect(rows()[0].textContent).toContain('model.user.enabled.true');
      expect(element.querySelector('p-badge')!.textContent?.trim()).toBe('2');
      expect(element.querySelector('.p-datatable-mask')).toBeNull();
    });

    it('should show the roles of the users', async () => {
      await setUp([{ ...mario, roles: [{ id: 'role-id' as UUID, name: 'ADMIN', system: true }] }]);

      expect(rows()[0].textContent).toContain('ADMIN');
      expect(rows()[0].textContent).not.toContain(
        'modules.user-management.pages.user-list.no-roles',
      );
    });

    it('should show that a user has no roles', async () => {
      await setUp([mario]);

      expect(rows()[0].textContent).toContain('modules.user-management.pages.user-list.no-roles');
    });

    it('should mute only the rows of the disabled users', async () => {
      await setUp([mario, { ...luigi, enabled: false }]);

      expect(rows()[0].classList).not.toContain('text-muted');
      expect(rows()[1].classList).toContain('text-muted');
    });

    it('should stop loading and show the empty state instead of the table when there are no users', async () => {
      await setUp(null);

      expect(element.querySelector('.p-datatable-mask')).toBeNull();
      expect(element.textContent).toContain(`${LIST}.empty.title`);
      expect(element.textContent).toContain(`${LIST}.empty.no-users`);
      expect(button(`${LIST}.empty.clear-filters`)).toBeUndefined();
      expect(element.querySelector('p-table')!.parentElement!.classList).toContain('hidden');
    });

    it('should open the creation form from the empty state', async () => {
      await setUp(null);
      const emptyState = element.querySelector<HTMLElement>('.pi-users')!.parentElement!;

      await click(button(`${LIST}.new-user`, emptyState));

      expect(dialogService.open).toHaveBeenCalledOnce();
    });

    it('should reload the users filtered by the typed text', async () => {
      await setUp();

      await search('mar');

      expect(lastCriteria().filter.text).toBe('mar');
    });

    it('should offer to clear the filter when the search finds no users', async () => {
      await setUp();
      userService.getUsers.mockReturnValue(page([]));
      await search('zzz');

      expect(element.textContent).toContain(`${LIST}.empty.filtered`);
      userService.getUsers.mockReturnValue(page([mario, luigi]));

      await click(button(`${LIST}.empty.clear-filters`));

      expect(lastCriteria().filter.text).toBe('');
      expect(lastCriteria().page).toBe(0);
      expect(element.querySelector<HTMLInputElement>('p-iconfield input')!.value).toBe('');
      expect(rows()).toHaveLength(2);
      expect(element.textContent).not.toContain(`${LIST}.empty.title`);
    });

    it('should load the page and the page size requested by the paginator', async () => {
      await setUp([mario, luigi], 60);

      await click(element.querySelector<HTMLButtonElement>('.p-paginator-next')!);

      expect(lastCriteria().page).toBe(1);
      expect(lastCriteria().size).toBe(20);
    });
  });

  describe('detail', () => {
    it('should not show the detail until a user is selected', async () => {
      await setUp();

      expect(detail()).toBeNull();
    });

    it('should show the detail of the clicked user', async () => {
      await setUp();

      await selectRow(1);

      expect(detail()!.textContent).toContain('luigi@test.it');
    });

    it('should close the detail when the selected user is clicked again', async () => {
      await setUp();
      await selectRow(1);

      await selectRow(1);

      expect(detail()).toBeNull();
    });

    it('should close the detail when it is dismissed', async () => {
      await setUp();
      await selectRow(1);

      await click(button('general.button.label.close', detail()!));

      expect(detail()).toBeNull();
    });
  });

  describe('add', () => {
    it('should open the user form without a user', async () => {
      await setUp();

      await click(button(`${LIST}.new-user`));

      const [component, config] = dialogService.open.mock.lastCall!;
      expect(component).toBe(UserFormComponent);
      expect(config.data.userId).toBeUndefined();
    });

    it('should show a message and reload the first page when a user is created', async () => {
      await setUp([mario, luigi], 60);
      await click(element.querySelector<HTMLButtonElement>('.p-paginator-next')!);
      await click(button(`${LIST}.new-user`));
      const loadsBefore = userService.getUsers.mock.calls.length;

      formDialogClose.next(luigi);
      await fixture.whenStable();

      expect(messages).toEqual([{ severity: 'success', detail: `${LIST}.creation-message` }]);
      expect(userService.getUsers.mock.calls.length).toBe(loadsBefore + 1);
      expect(lastCriteria().offset).toBe(0);
      expect(accountService.reload).not.toHaveBeenCalled();
    });

    it('should do nothing when the user form is closed without result', async () => {
      await setUp();
      await click(button(`${LIST}.new-user`));
      const loadsBefore = userService.getUsers.mock.calls.length;

      formDialogClose.next(undefined);

      expect(messages).toEqual([]);
      expect(userService.getUsers.mock.calls.length).toBe(loadsBefore);
    });
  });

  describe('edit', () => {
    it('should open the user form with the user of the detail', async () => {
      await setUp();
      await selectRow(1);

      await click(button('modules.user-management.pages.user-detail.edit', detail()!));

      const config = dialogService.open.mock.lastCall![1];
      expect(config.data.userId).toBe(luigi.id);
    });

    it('should reload the page and keep the detail updated when a user is edited', async () => {
      await setUp();
      await selectRow(1);
      await click(button('modules.user-management.pages.user-detail.edit', detail()!));
      const updated = { ...luigi, email: 'luigi@new.it' };
      userService.getUsers.mockReturnValue(page([mario, updated]));

      formDialogClose.next(updated);
      await fixture.whenStable();

      expect(messages).toEqual([{ severity: 'success', detail: `${LIST}.edit-message` }]);
      expect(detail()!.textContent).toContain('luigi@new.it');
      expect(accountService.reload).not.toHaveBeenCalled();
    });

    it('should reload the current account when the current account is edited', async () => {
      await setUp();
      await selectRow(0);
      await click(button('modules.user-management.pages.user-detail.edit', detail()!));

      formDialogClose.next(mario);
      await fixture.whenStable();

      expect(accountService.reload).toHaveBeenCalledOnce();
    });
  });

  describe('delete', () => {
    it('should delete the user after confirmation, show a message and close the detail', async () => {
      await setUp();
      await selectRow(1);
      await click(button('general.button.label.more', detail()!));
      await click(content(menuItem('general.button.label.delete')));
      expect(confirmDialog.confirm).toHaveBeenCalledWith(
        `${LIST}.confirmation-delete-dialog.title`,
        `${LIST}.confirmation-delete-dialog.message`,
        expect.any(Function),
      );
      expect(userService.deleteUser).not.toHaveBeenCalled();
      userService.getUsers.mockReturnValue(page([mario]));

      confirmDialog.confirm.mock.lastCall![2]();
      await fixture.whenStable();

      expect(userService.deleteUser).toHaveBeenCalledWith(luigi.id);
      expect(messages).toEqual([{ severity: 'success', detail: `${LIST}.delete-message` }]);
      expect(rows()).toHaveLength(1);
      expect(detail()).toBeNull();
    });
  });
});
