import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { debounceTime, retry, Subject } from 'rxjs';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { InputText } from 'primeng/inputtext';
import { Button } from 'primeng/button';
import { InputIcon } from 'primeng/inputicon';
import { IconField } from 'primeng/iconfield';
import { Avatar } from 'primeng/avatar';
import { Tag } from 'primeng/tag';
import { Badge } from 'primeng/badge';
import { DialogService } from 'primeng/dynamicdialog';
import { MessageService } from 'primeng/api';
import { UserService } from 'app/modules/user-management/api/services/user.service';
import { UserResponse } from 'app/modules/user-management/api/generated';
import { UserCriteria } from 'app/modules/user-management/api/user-criteria';
import {
  UserFormComponent,
  UserFormConfig,
} from 'app/modules/user-management/pages/user-form/user-form.component';
import { UserDetailComponent } from 'app/modules/user-management/pages/user-detail/user-detail.component';
import { AccountService } from 'app/core/api/services/account.service';
import { HttpHeader } from 'app/core/api/http-header';
import { MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS } from 'app/core/utils/constants/retry';
import { FORM_DIALOG_CONFIG } from 'app/core/utils/constants/dialog';
import { TableRowDirective } from 'app/core/utils/directives/table-row.directive';
import { ConfirmDialogService } from 'app/core/utils/services/confirm-dialog.service';
import { getFullName, getInitials } from 'app/core/utils/functions/person-name';

@Component({
  selector: 'app-user-list',
  imports: [
    TableModule,
    TranslatePipe,
    TableRowDirective,
    InputText,
    Button,
    InputIcon,
    IconField,
    Avatar,
    Tag,
    Badge,
    UserDetailComponent,
    FormsModule,
  ],
  templateUrl: './user-list.component.html',
  host: { class: 'flex h-full flex-col gap-6' },
})
export class UserListComponent {
  private readonly dialogService = inject(DialogService);
  private readonly translateService = inject(TranslateService);
  private readonly messageService = inject(MessageService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly userService = inject(UserService);
  private readonly accountService = inject(AccountService);

  protected readonly getFullName = getFullName;
  protected readonly getInitials = getInitials;

  protected readonly loading = signal(true);
  protected readonly users = signal<UserResponse[]>([]);
  protected readonly selectedUser = signal<UserResponse | null>(null);
  protected readonly totalRecords = signal(0);
  protected readonly first = signal(0);

  protected readonly filterSubject = new Subject<void>();
  protected readonly criteria = new UserCriteria(0, 20);

  constructor() {
    this.filterSubject
      .pipe(debounceTime(200), takeUntilDestroyed())
      .subscribe(() => this.loadPage());
  }

  protected onLazyLoad(event: TableLazyLoadEvent) {
    this.criteria.offset = event.first ?? 0;
    this.criteria.limit = event.rows ?? this.criteria.limit;

    this.loadPage();
  }

  private loadPage() {
    this.loading.set(true);

    this.userService
      .getUsers(this.criteria)
      .pipe(retry({ count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS }))
      .subscribe({
        next: (response) => {
          const users = response.body ?? [];
          this.users.set(users);
          this.selectedUser.update(
            (selected) => selected && (users.find((u) => u.id === selected.id) ?? null),
          );
          this.totalRecords.set(Number(response.headers.get(HttpHeader.TotalCount)));
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  private loadFirstPage() {
    this.criteria.offset = 0;
    this.first.set(0);

    this.loadPage();
  }

  protected onAddClick() {
    this.dialogService
      .open<UserFormComponent, UserFormConfig>(UserFormComponent, {
        ...FORM_DIALOG_CONFIG,
        data: {},
      })
      ?.onClose.subscribe((created: UserResponse | undefined) => {
        if (!created) {
          return;
        }

        this.showSuccess('modules.user-management.pages.user-list.creation-message');
        this.loadFirstPage();
      });
  }

  protected onEdit(user: UserResponse) {
    this.dialogService
      .open<UserFormComponent, UserFormConfig>(UserFormComponent, {
        ...FORM_DIALOG_CONFIG,
        data: { userId: user.id },
      })
      ?.onClose.subscribe((updated: UserResponse | undefined) => {
        if (!updated) {
          return;
        }

        if (user.username === this.accountService.account()?.username) {
          this.accountService.reload();
        }

        this.showSuccess('modules.user-management.pages.user-list.edit-message');
        this.loadPage();
      });
  }

  protected onRemove(user: UserResponse) {
    this.confirmDialog.confirm(
      'modules.user-management.pages.user-list.confirmation-delete-dialog.title',
      'modules.user-management.pages.user-list.confirmation-delete-dialog.message',
      () => this.deleteUser(user),
    );
  }

  private deleteUser(user: UserResponse) {
    this.userService.deleteUsers([user]).subscribe({
      next: () => {
        this.showSuccess('modules.user-management.pages.user-list.delete-message');
        this.loadPage();
      },
      error: () => this.loadPage(),
    });
  }

  private showSuccess(key: string) {
    this.messageService.add({ severity: 'success', detail: this.translateService.instant(key) });
  }
}
