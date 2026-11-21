import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { retry } from 'rxjs';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { Password } from 'primeng/password';
import { Button } from 'primeng/button';
import { Listbox } from 'primeng/listbox';
import { Tag } from 'primeng/tag';
import { Avatar } from 'primeng/avatar';
import { ToggleSwitch } from 'primeng/toggleswitch';
import { SelectButton } from 'primeng/selectbutton';
import { Divider } from 'primeng/divider';
import { Message } from 'primeng/message';
import { ProgressSpinner } from 'primeng/progressspinner';
import { SectionTitleComponent } from 'app/core/components/section-title/section-title.component';
import { MessageService } from 'primeng/api';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Gender, Language } from 'app/core/api/generated';
import { UUID } from 'app/core/api/models/uuid';
import { MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS } from 'app/core/utils/constants/retry';
import { DEFAULT_LANGUAGE, LANGUAGES } from 'app/core/utils/constants/languages';
import { ConfirmDialogService } from 'app/core/utils/services/confirm-dialog.service';
import { getFullName, getInitials } from 'app/core/utils/functions/person-name';
import {
  RoleSummary,
  UserCreateRequest,
  UserResponse,
  UserUpdateRequest,
} from 'app/modules/user-management/api/generated';
import { UserService } from 'app/modules/user-management/api/services/user.service';
import { RoleService } from 'app/modules/user-management/api/services/role.service';

export type UserFormConfig = { userId?: UUID };

const ERRORS = 'modules.user-management.pages.user-form.errors';

const PATTERN_ERRORS: Record<string, string> = {
  taxIdNumber: `${ERRORS}.tax-id-number`,
  mobileNumber: `${ERRORS}.mobile-number`,
};

function passwordsMatch(form: AbstractControl): ValidationErrors | null {
  const { password, passwordConfirmation } = form.value;
  return password === passwordConfirmation ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-user-form',
  imports: [
    TranslatePipe,
    InputText,
    ReactiveFormsModule,
    Select,
    Password,
    Button,
    Listbox,
    Tag,
    Avatar,
    ToggleSwitch,
    SelectButton,
    Divider,
    Message,
    ProgressSpinner,
    SectionTitleComponent,
  ],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.css',
  host: { class: 'flex min-h-0 flex-1 flex-col' },
})
export class UserFormComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly config = inject<DynamicDialogConfig<UserFormConfig>>(DynamicDialogConfig);
  private readonly ref = inject(DynamicDialogRef);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly translateService = inject(TranslateService);
  private readonly userService = inject(UserService);
  private readonly roleService = inject(RoleService);
  private readonly messageService = inject(MessageService);

  protected readonly userForm = this.formBuilder.nonNullable.group(
    {
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
      passwordConfirmation: ['', Validators.required],
      roleIds: this.formBuilder.nonNullable.control<UUID[]>([]),
      enabled: [false],
    },
    { validators: passwordsMatch },
  );
  private initialFormValue = this.currentFormValue();

  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly userEdited = signal<UserResponse | undefined>(undefined);
  protected readonly isUserEdit = this.config.data?.userId !== undefined;
  protected readonly fullName = computed(() => {
    const user = this.userEdited();
    return user ? getFullName(user.firstname, user.lastname) : '';
  });
  protected readonly initials = computed(() => {
    const user = this.userEdited();
    return user ? getInitials(user.firstname, user.lastname) : '';
  });

  protected readonly enabled = toSignal(this.userForm.controls.enabled.valueChanges, {
    initialValue: this.userForm.controls.enabled.value,
  });
  private readonly selectedRoleIds = toSignal(this.userForm.controls.roleIds.valueChanges, {
    initialValue: this.userForm.controls.roleIds.value,
  });
  protected readonly roles = signal<RoleSummary[]>([]);
  protected readonly selectedRolesCount = computed(() => this.selectedRoleIds().length);

  protected readonly languages = LANGUAGES;
  protected readonly genders = [
    { value: Gender.M, label: 'model.gender.male' },
    { value: Gender.F, label: 'model.gender.female' },
  ];

  constructor() {
    this.roleService
      .getRoles()
      .pipe(retry({ count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS }))
      .subscribe({
        next: (roles) => {
          if (roles?.length) {
            this.roles.set(roles);
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

          this.userForm.controls.password.disable();
          this.userForm.controls.passwordConfirmation.disable();
          this.initialFormValue = this.currentFormValue();

          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  protected errorOf(field: keyof typeof this.userForm.controls): string | null {
    const control = this.userForm.controls[field];
    if (!control.touched) {
      return null;
    }

    if (control.hasError('required')) {
      return `${ERRORS}.required`;
    }

    if (control.hasError('email')) {
      return `${ERRORS}.email`;
    }

    if (control.hasError('pattern')) {
      return PATTERN_ERRORS[field] ?? null;
    }

    if (field === 'passwordConfirmation' && this.userForm.hasError('passwordMismatch')) {
      return `${ERRORS}.password-mismatch`;
    }

    return null;
  }

  protected onSaveClick() {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
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

    this.saving.set(true);
    request.subscribe({
      next: (user) => this.ref.close(user),
      error: () => this.saving.set(false),
    });
  }

  private currentFormValue(): string {
    const value = this.userForm.getRawValue();
    return JSON.stringify({ ...value, roleIds: [...value.roleIds].sort() });
  }

  protected onCancelClick() {
    if (this.currentFormValue() === this.initialFormValue) {
      this.ref.close();
      return;
    }

    this.confirmDialog.confirm(
      'modules.user-management.pages.user-form.confirmation-cancel-dialog.title',
      'modules.user-management.pages.user-form.confirmation-cancel-dialog.message',
      () => this.ref.close(),
    );
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
      roleIds: user.roles.map((r) => r.id),
      enabled: user.enabled,
    });
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
