import { Component, effect, inject, signal } from '@angular/core';
import { UpperCasePipe } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Select } from 'primeng/select';
import { Button } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { AccountService } from 'app/core/api/services/account.service';
import { Account, AccountPreferencesUpdate, Language } from 'app/core/api/generated';
import { LANGUAGES } from 'app/core/utils/constants/languages';

@Component({
  selector: 'app-account-detail',
  imports: [
    ProgressSpinner,
    TranslatePipe,
    UpperCasePipe,
    FormsModule,
    ReactiveFormsModule,
    Select,
    Button,
  ],
  templateUrl: './account-detail.component.html',
  styleUrl: './account-detail.component.css',
})
export class AccountDetailComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly messageService = inject(MessageService);
  private readonly translateService = inject(TranslateService);
  private readonly accountService = inject(AccountService);

  protected readonly preferencesForm = this.formBuilder.nonNullable.group({
    language: this.formBuilder.nonNullable.control<Language | null>(null, Validators.required),
  });

  protected readonly account = this.accountService.account;
  protected readonly editing = signal(false);
  protected readonly languages = LANGUAGES;

  constructor() {
    effect(() => {
      const account = this.account();

      if (account) {
        this.resetForm(account);
      }
    });
  }

  private resetForm(account: Account) {
    this.editing.set(false);
    this.preferencesForm.patchValue({
      language: account.preferences.language,
    });
    this.preferencesForm.disable();
  }

  protected onEditClick() {
    this.editing.set(true);
    this.preferencesForm.enable();
  }

  protected onCancelClick() {
    this.resetForm(this.account()!);
  }

  protected onSaveClick() {
    const preferences: AccountPreferencesUpdate = {
      language: this.preferencesForm.getRawValue().language!,
    };

    this.accountService.updateCurrentAccountPreferences(preferences).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: this.translateService.instant(
            'core.pages.account-detail.edit-preferences-message',
          ),
        });

        globalThis.location.reload();
      },
      error: () => undefined,
    });
  }
}
