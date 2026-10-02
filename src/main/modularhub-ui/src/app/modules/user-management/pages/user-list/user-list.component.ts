import {Component, signal} from '@angular/core';
import {TableModule, TablePassThrough} from 'primeng/table';
import {UserService} from 'app/modules/user-management/api/services/user.service';
import {User} from 'app/modules/user-management/api/models/user';
import {MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS} from 'app/app.config';
import {BehaviorSubject, debounceTime, retry, Subject} from 'rxjs';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {TableRowDirective} from 'app/core/utils/directives/table-row.directive';
import {InputText} from 'primeng/inputtext';
import {Button} from 'primeng/button';
import {NgClass, UpperCasePipe} from '@angular/common';
import {InputIcon} from 'primeng/inputicon';
import {IconField} from 'primeng/iconfield';
import {UserCriteria} from 'app/modules/user-management/api/user-criteria';
import {DialogService} from 'primeng/dynamicdialog';
import {UserFormComponent} from 'app/modules/user-management/pages/user-form/user-form.component';
import {
  UserFormFooterComponent
} from 'app/modules/user-management/pages/user-form/user-form-footer/user-form-footer.component';
import {ConfirmationService, MessageService} from 'primeng/api';
import {HttpHeader} from 'app/core/api/http-header';
import {UserDetailComponent} from 'app/modules/user-management/pages/user-detail/user-detail.component';
import {FormsModule} from '@angular/forms';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AccountService} from 'app/core/api/services/account.service';
import {Account} from 'app/core/api/models/account';
import {Tooltip} from 'primeng/tooltip';

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
    Tooltip
  ],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.css',
})
export class UserListComponent {

  protected tablePT: TablePassThrough = {
    thead: {
      class: '[&_th]:overflow-hidden [&_th]:text-ellipsis [&_th]:whitespace-nowrap'
    },
    tbody: {
      class: '[&_td]:overflow-hidden [&_td]:text-ellipsis [&_td]:whitespace-nowrap'
    }
  }

  protected loading = signal(true);
  private currentAccount?: Account;

  protected users: User[] = [];
  protected selectedUsers: User[] = [];
  protected totalRecords = 0;
  protected first: number = 0;

  protected readonly filterSubject = new Subject<void>();
  protected criteria: UserCriteria = new UserCriteria(0, 20);

  constructor(
    private readonly dialogService: DialogService,
    private readonly translateService: TranslateService,
    private readonly messageService: MessageService,
    private readonly confirmationService: ConfirmationService,
    private readonly userService: UserService,
    private readonly accountService: AccountService
  ) {
    this.filterSubject.pipe(
      debounceTime(200),
      takeUntilDestroyed()
    ).subscribe(value => {
      this.loadPage();
    });
  }

  protected loadPage(event?: any) {
    if (event) {
      this.criteria.offset = event.first;
      this.criteria.limit = event.rows;
    }

    this.loading.set(true);

    this.userService.getUsers(this.criteria)
      .pipe(retry({count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS}))
      .subscribe({
        next: response => {
          if (!response.body) {
            return;
          }

          this.users = response.body;
          this.totalRecords = Number(response.headers.get(HttpHeader.TotalCount));

          this.loading.set(false);
        },
        error: err => {
          this.loading.set(false);
        }
      });

    this.accountService.getObservableAccount()
      .pipe(retry({count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS}))
      .subscribe({
        next: (account) => {
          if (!account) {
            return;
          }

          this.currentAccount = account;
        },
        error: () => {
        }
      });

  }

  protected isEditButtonEnabled() {
    return this.selectedUsers.length === 1;
  }

  protected isDeleteButtonEnabled() {
    return this.selectedUsers.length > 0
      && !this.isCurrentAccountSelected();
  }

  protected isCurrentAccountSelected() {
    return this.selectedUsers.some(u => u.username === this.currentAccount?.username);
  }

  protected onAddButtonClick() {
    this.openUserFormDialog();
  }

  protected onEditButtonClick() {
    this.openUserFormDialog(this.selectedUsers[0]);
  }

  private openUserFormDialog(user?: User) {
    let header: string;
    let detail: string;

    if (user) {
      const fullName = user.firstname + ' ' + user.lastname;
      header = this.translateService.instant('modules.user-management.pages.user-form.edit-title', {
        fullName: fullName,
      });
      detail = this.translateService.instant('modules.user-management.pages.user-list.edit-message');
    } else {
      header = this.translateService.instant('modules.user-management.pages.user-form.creation-title');
      detail = this.translateService.instant('modules.user-management.pages.user-list.creation-message');
    }

    this.dialogService.open(UserFormComponent, {
      header: header,
      width: '60vw',
      data: {
        buttonClick: new Subject<string>(),
        formValid: new BehaviorSubject(false),
        userId: user?.id,
      },
      templates: {
        footer: UserFormFooterComponent,
      }
    })?.onClose.subscribe((result: User) => {
      if (!result) {
        return;
      }

      if (user && user.username == this.currentAccount?.username) {
        this.accountService.reload();
      }

      this.messageService.add({severity: 'success', detail});
      this.reset();
    });
  }

  private reset() {
    this.selectedUsers = [];

    this.criteria.offset = 0;
    this.first = 0;

    this.loadPage();
  }

  protected onDeleteButtonClick() {
    this.confirmationService.confirm({
      header: this.translateService.instant('modules.user-management.pages.user-list.confirmation-delete-dialog.title'),
      message: this.translateService.instant('modules.user-management.pages.user-list.confirmation-delete-dialog.message'),
      closable: true,
      closeOnEscape: true,
      icon: 'pi pi-exclamation-triangle',
      rejectButtonProps: {
        label: this.translateService.instant('general.button.label.cancel'),
        severity: 'secondary',
        outlined: true,
      },
      acceptButtonProps: {
        label: this.translateService.instant('general.button.label.confirm'),
        severity: 'secondary',
      },
      accept: () => {
        this.userService.deleteUsers(this.selectedUsers).subscribe({
          next: (deleted) => {
            this.selectedUsers = [];
            this.messageService.add({
              severity: 'success',
              summary: this.translateService.instant('modules.user-management.pages.user-list.delete-message')
            });

            this.loadPage();
          },
          error: (err) => {
            this.loadPage();
          }
        });
      },
    });
  }

  protected isUserDetailVisible() {
    return this.selectedUsers.length === 1;
  }
}
