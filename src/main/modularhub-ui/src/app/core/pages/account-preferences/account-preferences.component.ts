import { Component, computed, effect, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { Select } from 'primeng/select';
import { Button } from 'primeng/button';
import { AccountService } from 'app/core/api/services/account.service';
import { Account, Language } from 'app/core/api/generated';
import { LANGUAGES } from 'app/core/utils/constants/languages';

@Component({
  selector: 'app-account-preferences',
  imports: [ReactiveFormsModule, Select, TranslatePipe, Button],
  templateUrl: './account-preferences.component.html',
  styleUrl: './account-preferences.component.css',
})
export class AccountPreferencesComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly account = inject(AccountService).account;

  protected readonly preferencesForm = this.formBuilder.nonNullable.group({
    language: this.formBuilder.nonNullable.control<Language | null>(null, Validators.required),
  });

  protected readonly loading = computed(() => !this.account());
  protected editing = false;

  protected readonly languages = LANGUAGES;

  constructor() {
    effect(() => {
      const account = this.account();

      if (account) {
        this.initForm(account);
      }
    });
  }

  private initForm(account: Account) {
    this.preferencesForm.patchValue({
      language: account.preferences.language,
    });

    this.preferencesForm.disable();
  }

  protected onCancel() {}

  protected onSave() {}
}
