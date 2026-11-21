import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NgClass, UpperCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BehaviorSubject, debounceTime, retry, Subject } from 'rxjs';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { TableLazyLoadEvent, TableModule, TablePassThrough } from 'primeng/table';
import { InputText } from 'primeng/inputtext';
import { Button } from 'primeng/button';
import { InputIcon } from 'primeng/inputicon';
import { IconField } from 'primeng/iconfield';
import { Tooltip } from 'primeng/tooltip';
import { DialogService } from 'primeng/dynamicdialog';
import { MessageService } from 'primeng/api';
import { UserService } from 'app/modules/user-management/api/services/user.service';
import { UserResponse } from 'app/modules/user-management/api/generated';
import { UserCriteria } from 'app/modules/user-management/api/user-criteria';
import { UserFormComponent } from 'app/modules/user-management/pages/user-form/user-form.component';
import { UserFormFooterComponent } from 'app/modules/user-management/pages/user-form/user-form-footer/user-form-footer.component';
import { UserDetailComponent } from 'app/modules/user-management/pages/user-detail/user-detail.component';
import { AccountService } from 'app/core/api/services/account.service';
import { HttpHeader } from 'app/core/api/http-header';
import { MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS } from 'app/core/utils/constants/retry';
import { TableRowDirective } from 'app/core/utils/directives/table-row.directive';
import { ConfirmDialogService } from 'app/core/utils/services/confirm-dialog.service';
import { getFullName } from 'app/core/utils/functions/person-name';

@Component({
  selector: 'app-user-list',
  imports: [
    TableModule,
    TranslatePipe,
    TableRowDirective,
    InputText,
    Button,
    UpperCasePipe,
    InputIcon,
    IconField,
    UserDetailComponent,
    FormsModule,
    NgClass,
    Tooltip,
  ],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.css',
})
export class UserListComponent {
  private readonly dialogService = inject(DialogService);
  private readonly translateService = inject(TranslateService);
  private readonly messageService = inject(MessageService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly userService = inject(UserService);
  private readonly accountService = inject(AccountService);

  protected readonly tablePT: TablePassThrough = {
    thead: {
      class: '[&_th]:overflow-hidden [&_th]:text-ellipsis [&_th]:whitespace-nowrap',
    },
    tbody: {
      class: '[&_td]:overflow-hidden [&_td]:text-ellipsis [&_td]:whitespace-nowrap',
    },
  };

  protected readonly loading = signal(true);
  protected readonly users = signal<UserResponse[]>([]);
  protected readonly selectedUsers = signal<UserResponse[]>([]);
  protected readonly totalRecords = signal(0);
  protected readonly first = signal(0);

  protected readonly singleUserSelected = computed(() => this.selectedUsers().length === 1);
  protected readonly selectedUserId = computed(() => this.selectedUsers()[0]?.id);
  protected readonly isCurrentAccountSelected = computed(() =>
    this.selectedUsers().some((u) => u.username === this.accountService.account()?.username),
  );
  protected readonly isDeleteButtonEnabled = computed(
    () => this.selectedUsers().length > 0 && !this.isCurrentAccountSelected(),
  );

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
          this.users.set(response.body ?? []);
          this.totalRecords.set(Number(response.headers.get(HttpHeader.TotalCount)));
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  protected onAddButtonClick() {
    this.openUserFormDialog();
  }

  protected onEditButtonClick() {
    this.openUserFormDialog(this.selectedUsers()[0]);
  }

  private openUserFormDialog(user?: UserResponse) {
    let header: string;
    let detail: string;

    if (user) {
      header = this.translateService.instant('modules.user-management.pages.user-form.edit-title', {
        fullName: getFullName(user.firstname, user.lastname),
      });
      detail = this.translateService.instant(
        'modules.user-management.pages.user-list.edit-message',
      );
    } else {
      header = this.translateService.instant(
        'modules.user-management.pages.user-form.creation-title',
      );
      detail = this.translateService.instant(
        'modules.user-management.pages.user-list.creation-message',
      );
    }

    this.dialogService
      .open(UserFormComponent, {
        header,
        width: '60vw',
        data: {
          buttonClick: new Subject<string>(),
          formValid: new BehaviorSubject(false),
          userId: user?.id,
        },
        templates: {
          footer: UserFormFooterComponent,
        },
      })
      ?.onClose.subscribe((result: UserResponse | undefined) => {
        if (!result) {
          return;
        }

        if (user && user.username === this.accountService.account()?.username) {
          this.accountService.reload();
        }

        this.messageService.add({ severity: 'success', detail });
        this.reset();
      });
  }

  private reset() {
    this.selectedUsers.set([]);
    this.criteria.offset = 0;
    this.first.set(0);

    this.loadPage();
  }

  protected onDeleteButtonClick() {
    this.confirmDialog.confirm(
      'modules.user-management.pages.user-list.confirmation-delete-dialog.title',
      'modules.user-management.pages.user-list.confirmation-delete-dialog.message',
      () => this.deleteSelectedUsers(),
    );
  }

  private deleteSelectedUsers() {
    this.userService.deleteUsers(this.selectedUsers()).subscribe({
      next: () => {
        this.selectedUsers.set([]);
        this.messageService.add({
          severity: 'success',
          summary: this.translateService.instant(
            'modules.user-management.pages.user-list.delete-message',
          ),
        });

        this.loadPage();
      },
      error: () => this.loadPage(),
    });
  }
}
