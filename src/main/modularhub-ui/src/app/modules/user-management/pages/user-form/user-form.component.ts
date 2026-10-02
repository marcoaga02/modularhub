import {Component, inject, signal} from '@angular/core';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {InputText} from 'primeng/inputtext';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {Checkbox} from 'primeng/checkbox';
import {Gender} from 'app/core/api/models/gender';
import {RadioButton} from 'primeng/radiobutton';
import {LanguageService} from 'app/modules/user-management/api/services/language.service';
import {BehaviorSubject, forkJoin, retry, Subject} from 'rxjs';
import {MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS} from 'app/app.config';
import {Select} from 'primeng/select';
import {DialogService, DynamicDialogConfig, DynamicDialogRef} from 'primeng/dynamicdialog';
import {
  UserFormFooterConfig
} from 'app/modules/user-management/pages/user-form/user-form-footer/user-form-footer.component';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ConfirmationService, MessageService} from 'primeng/api';
import {User, UserRequest} from 'app/modules/user-management/api/models/user';
import {UserService} from 'app/modules/user-management/api/services/user.service';
import {Password} from 'primeng/password';
import {NgClass} from '@angular/common';
import {GroupSelectorComponent} from 'app/modules/user-management/pages/group-selector/group-selector.component';
import {GroupService} from 'app/modules/user-management/api/services/group.service';
import {Group} from 'app/core/api/models/group';
import {DialogFooterComponent} from 'app/core/components/dialog-footer/dialog-footer.component';
import {UUID} from 'app/core/api/models/uuid';
import {Button} from 'primeng/button';
import {Tooltip} from 'primeng/tooltip';

type LanguageWithValue = {
  label: string,
  value: string
}

@Component({
  selector: 'app-user-form',
  imports: [
    TranslatePipe,
    InputText,
    Checkbox,
    ReactiveFormsModule,
    RadioButton,
    Select,
    Password,
    NgClass,
    Button,
    Tooltip
  ],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.css',
})
export class UserFormComponent {

  private readonly formBuilder = inject(FormBuilder);

  protected userForm = this.formBuilder.nonNullable.group({
    firstname: ['', Validators.required],
    lastname: ['', Validators.required],
    gender: this.formBuilder.nonNullable.control<Gender | null>(null, Validators.required),
    language: this.formBuilder.nonNullable.control<UUID | null>(null, Validators.required),
    mobileNumber: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
    taxIdNumber: ['', [Validators.required, Validators.pattern(/^[A-Za-z]{6}\d{2}[A-Za-z]\d{2}[A-Za-z]\d{3}[A-Za-z]$/)]],
    email: ['', [Validators.required, Validators.email]],
    username: ['', Validators.required],
    password: ['', Validators.required],
    groupsIds: this.formBuilder.nonNullable.array<UUID>([]),
    enabled: [false, Validators.required],
  });

  protected loading = signal(true);
  protected userEdited?: User;

  protected languages: LanguageWithValue[] = [];
  private groups: Group[] = [];

  constructor(
    private readonly config: DynamicDialogConfig<UserFormFooterConfig & {userId?: string}>,
    private readonly ref: DynamicDialogRef,
    private readonly confirmationService: ConfirmationService,
    private readonly translateService: TranslateService,
    private readonly dialogService: DialogService,
    private readonly languageService: LanguageService,
    private readonly userService: UserService,
    private readonly groupService: GroupService,
    private readonly messageService: MessageService
  ) {
    this.config.data!.buttonClick
      .pipe(takeUntilDestroyed())
      .subscribe((action) => {
        switch (action) {
          case "confirm":
            this.confirm();
            break;
          case "cancel":
            this.cancel();
            break;
          case "groups":
            this.showGroupsSelectionDialog();
            break;
        }
      });

    this.userForm.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe(
        () => this.config.data!.formValid.next(this.userForm.valid)
      );

    forkJoin([
      this.languageService.getLanguages().pipe(
        retry({count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS}),
      ),
      this.groupService.getGroups().pipe(
        retry({count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS}),
      ),
    ]).subscribe({
      next: ([languages, groups]) => {
        if (languages?.length) {
          this.languages = languages.map(l => ({
              label: l.label,
              value: l.id
            })
          );

          const defaultLanguage = languages.find(l => l.isDefault) || languages.pop();
          if (defaultLanguage) {
            this.userForm.controls.language.setValue(defaultLanguage.id);
          }
        }

        if (groups?.length) {
          this.groups = groups;
        }

        if (this.config.data?.userId) {
          this.userService.getUserById(this.config.data.userId).pipe(
            retry({count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS}),
          ).subscribe({
            next: user => {
              if (!user) {
                return;
              }

              this.userEdited = user;
              this.initForm(user);

              this.userForm.controls.password.clearValidators();
              this.userForm.controls.password.updateValueAndValidity();
              this.userForm.controls.password.disable();
            },
            error: () => {
            }
          });
        }

        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  protected isUserEdit() {
    return this.userEdited !== undefined;
  }

  private confirm() {
    if (!this.userForm.valid) {
      this.userForm.markAllAsTouched();
      this.userForm.markAllAsDirty();
      return;
    }

    const formValues = this.userForm.getRawValue();
    const userReq: UserRequest = {
      firstname: formValues.firstname,
      lastname: formValues.lastname,
      gender: formValues.gender!,
      languageId: formValues.language!,
      mobileNumber: formValues.mobileNumber,
      taxIdNumber: formValues.taxIdNumber,
      email: formValues.email,
      username: formValues.username,
      password: formValues.password,
      groupIds: formValues.groupsIds,
      enabled: formValues.enabled,
    }

    if (this.userEdited) {
      this.userService.updateUser(this.userEdited.id!, userReq).subscribe({
        next: user => {
          this.ref.close(user);
        },
        error: () => {
        }
      });
    } else {
      this.userService.createUser(userReq).subscribe({
        next: user => {
          this.ref.close(user);
        },
        error: () => {
        }
      });
    }

  }

  private cancel() {
    this.confirmationService.confirm({
      header: this.translateService.instant('modules.user-management.pages.user-form.confirmation-cancel-dialog.title'),
      message: this.translateService.instant('modules.user-management.pages.user-form.confirmation-cancel-dialog.message'),
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
        this.ref.close()
      },
    });
  }

  private showGroupsSelectionDialog() {
    this.dialogService.open(GroupSelectorComponent, {
      header: this.translateService.instant('modules.user-management.pages.group-selector.title'),
      width: '45vw',
      height: '50vh',
      closable: true,
      closeOnEscape: true,
      data: {
        groups: [...this.groups],
        selectedGroupIds: this.userForm.controls.groupsIds.value,
        formSubmit: new Subject<void>(),
        formValid: new BehaviorSubject(true),
      },
      templates: {
        footer: DialogFooterComponent
      }
    })?.onClose.subscribe((groupIds: UUID[]) => {
      if (!groupIds?.length) {
        return;
      }

      this.setGroupIds(groupIds);
    });
  }

  private initForm(user: User) {
    this.userForm.patchValue({
      firstname: user.firstname,
      lastname: user.lastname,
      gender: user.gender,
      language: user.language!.id,
      mobileNumber: user.mobileNumber,
      taxIdNumber:user.taxIdNumber,
      email: user.email,
      username: user.username,
      enabled: user.enabled,
    });

    this.setGroupIds(user.groups.map(g => g.id));
  }

  private setGroupIds(groupIds: UUID[]) {
    const control = this.userForm.controls.groupsIds;
    control.clear();
    groupIds.forEach(id => control.push(this.formBuilder.nonNullable.control(id)));
  }

  protected onResetPasswordClick() {
    this.confirmationService.confirm({
      header: this.translateService.instant('modules.user-management.pages.user-form.reset-password.confirmation-title'),
      message: this.translateService.instant('modules.user-management.pages.user-form.reset-password.confirmation-message'),
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
        this.userService.resetPassword(this.userEdited!.id!).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: this.translateService.instant('modules.user-management.pages.user-form.reset-password.success')
            });
          },
          error: () => {
          }
        });
      },
    });
  }
}
