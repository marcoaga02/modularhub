import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NgClass } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { BehaviorSubject, retry, Subject } from 'rxjs';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { InputText } from 'primeng/inputtext';
import { Checkbox } from 'primeng/checkbox';
import { RadioButton } from 'primeng/radiobutton';
import { Select } from 'primeng/select';
import { Password } from 'primeng/password';
import { Button } from 'primeng/button';
import { Tooltip } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';
import { DialogService, DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Gender, Language } from 'app/core/api/generated';
import { UUID } from 'app/core/api/models/uuid';
import { MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS } from 'app/core/utils/constants/retry';
import { DEFAULT_LANGUAGE, LANGUAGES } from 'app/core/utils/constants/languages';
import { ConfirmDialogService } from 'app/core/utils/services/confirm-dialog.service';
import { DialogFooterComponent } from 'app/core/components/dialog-footer/dialog-footer.component';
import {
  RoleSummary,
  UserCreateRequest,
  UserResponse,
  UserUpdateRequest,
} from 'app/modules/user-management/api/generated';
import { UserService } from 'app/modules/user-management/api/services/user.service';
import { RoleService } from 'app/modules/user-management/api/services/role.service';
import { UserFormFooterConfig } from 'app/modules/user-management/pages/user-form/user-form-footer/user-form-footer.component';
import { RoleSelectorComponent } from 'app/modules/user-management/pages/role-selector/role-selector.component';

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
    Tooltip,
  ],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.css',
})
export class UserFormComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly config =
    inject<DynamicDialogConfig<UserFormFooterConfig & { userId?: UUID }>>(DynamicDialogConfig);
  private readonly ref = inject(DynamicDialogRef);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly translateService = inject(TranslateService);
  private readonly dialogService = inject(DialogService);
  private readonly userService = inject(UserService);
  private readonly roleService = inject(RoleService);
  private readonly messageService = inject(MessageService);

  protected readonly userForm = this.formBuilder.nonNullable.group({
    firstname: ['', Validators.required],
    lastname: ['', Validators.required],
    gender: this.formBuilder.nonNullable.control<Gender | null>(null, Validators.required),
    language: this.formBuilder.nonNullable.control<Language | null>(
      DEFAULT_LANGUAGE,
      Validators.required,
    ),
    mobileNumber: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
    taxIdNumber: [
      '',
      [
        Validators.required,
        Validators.pattern(/^[A-Za-z]{6}\d{2}[A-Za-z]\d{2}[A-Za-z]\d{3}[A-Za-z]$/),
      ],
    ],
    email: ['', [Validators.required, Validators.email]],
    username: ['', Validators.required],
    password: ['', Validators.required],
    roleIds: this.formBuilder.nonNullable.array<UUID>([]),
    enabled: [false, Validators.required],
  });

  protected readonly loading = signal(true);
  private readonly userEdited = signal<UserResponse | undefined>(undefined);
  protected readonly isUserEdit = computed(() => this.userEdited() !== undefined);

  protected readonly languages = LANGUAGES;
  private roles: RoleSummary[] = [];

  constructor() {
    this.config.data!.buttonClick.pipe(takeUntilDestroyed()).subscribe((action) => {
      switch (action) {
        case 'confirm':
          this.confirm();
          break;
        case 'cancel':
          this.cancel();
          break;
        case 'roles':
          this.showRolesSelectionDialog();
          break;
      }
    });

    this.userForm.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.config.data!.formValid.next(this.userForm.valid));

    this.roleService
      .getRoles()
      .pipe(retry({ count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS }))
      .subscribe({
        next: (roles) => {
          if (roles?.length) {
            this.roles = roles;
          }

          if (this.config.data?.userId) {
            this.loadUser(this.config.data.userId);
            return;
          }

          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  private loadUser(userId: UUID) {
    this.userService
      .getUserById(userId)
      .pipe(retry({ count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS }))
      .subscribe({
        next: (user) => {
          this.userEdited.set(user);
          this.initForm(user);

          this.userForm.controls.password.clearValidators();
          this.userForm.controls.password.updateValueAndValidity();
          this.userForm.controls.password.disable();

          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  private confirm() {
    if (!this.userForm.valid) {
      this.userForm.markAllAsTouched();
      this.userForm.markAllAsDirty();
      return;
    }

    const formValues = this.userForm.getRawValue();
    const userReq: UserUpdateRequest = {
      firstname: formValues.firstname,
      lastname: formValues.lastname,
      gender: formValues.gender!,
      language: formValues.language!,
      mobileNumber: formValues.mobileNumber,
      taxIdNumber: formValues.taxIdNumber,
      email: formValues.email,
      username: formValues.username,
      roleIds: formValues.roleIds,
      enabled: formValues.enabled,
    };

    const userEdited = this.userEdited();
    const request = userEdited
      ? this.userService.updateUser(userEdited.id, userReq)
      : this.userService.createUser({
          ...userReq,
          password: formValues.password,
        } satisfies UserCreateRequest);

    request.subscribe({
      next: (user) => this.ref.close(user),
      error: () => undefined,
    });
  }

  private cancel() {
    this.confirmDialog.confirm(
      'modules.user-management.pages.user-form.confirmation-cancel-dialog.title',
      'modules.user-management.pages.user-form.confirmation-cancel-dialog.message',
      () => this.ref.close(),
    );
  }

  private showRolesSelectionDialog() {
    this.dialogService
      .open(RoleSelectorComponent, {
        header: this.translateService.instant('modules.user-management.pages.role-selector.title'),
        width: '45vw',
        height: '50vh',
        closable: true,
        closeOnEscape: true,
        data: {
          roles: [...this.roles],
          selectedRoleIds: this.userForm.controls.roleIds.value,
          formSubmit: new Subject<void>(),
          formValid: new BehaviorSubject(true),
        },
        templates: {
          footer: DialogFooterComponent,
        },
      })
      ?.onClose.subscribe((roleIds: UUID[] | undefined) => {
        if (roleIds === undefined) {
          return;
        }

        this.setRoleIds(roleIds);
      });
  }

  private initForm(user: UserResponse) {
    this.userForm.patchValue({
      firstname: user.firstname,
      lastname: user.lastname,
      gender: user.gender,
      language: user.language,
      mobileNumber: user.mobileNumber,
      taxIdNumber: user.taxIdNumber,
      email: user.email,
      username: user.username,
      enabled: user.enabled,
    });

    this.setRoleIds(user.roles.map((r) => r.id));
  }

  private setRoleIds(roleIds: UUID[]) {
    const control = this.userForm.controls.roleIds;
    control.clear();
    roleIds.forEach((id) => control.push(this.formBuilder.nonNullable.control(id)));
  }

  protected onResetPasswordClick() {
    this.confirmDialog.confirm(
      'modules.user-management.pages.user-form.reset-password.confirmation-title',
      'modules.user-management.pages.user-form.reset-password.confirmation-message',
      () => this.resetPassword(),
    );
  }

  private resetPassword() {
    this.userService.resetPassword(this.userEdited()!.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: this.translateService.instant(
            'modules.user-management.pages.user-form.reset-password.success',
          ),
        });
      },
      error: () => undefined,
    });
  }
}
