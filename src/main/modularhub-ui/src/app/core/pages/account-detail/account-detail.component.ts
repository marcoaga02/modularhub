import {Component, inject, signal} from '@angular/core';
import {AccountService} from 'app/core/api/services/account.service';
import {first, retry} from 'rxjs';
import {MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS} from 'app/app.config';
import {Account, Language} from 'app/core/api/generated';
import {ProgressSpinner} from 'primeng/progressspinner';
import {UpperCasePipe} from '@angular/common';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {Select} from 'primeng/select';
import {Button} from 'primeng/button';
import {AccountPreferencesUpdate} from 'app/core/api/generated';
import {MessageService} from 'primeng/api';
import {LANGUAGES} from 'app/core/utils/constants/languages';

@Component({
  selector: 'app-account-detail',
  imports: [
    ProgressSpinner,
    TranslatePipe,
    UpperCasePipe,
    FormsModule,
    ReactiveFormsModule,
    Select,
    Button
  ],
  templateUrl: './account-detail.component.html',
  styleUrl: './account-detail.component.css',
})
export class AccountDetailComponent {

  private readonly formBuilder = inject(FormBuilder);

  protected preferencesForm = this.formBuilder.nonNullable.group({
    language: this.formBuilder.nonNullable.control<Language | null>(null, Validators.required),
  });

  protected loading = signal(true);
  protected editing: boolean = false;

  protected currentAccount?: Account;
  protected readonly languages = LANGUAGES;

  constructor(
    private readonly messageService: MessageService,
    private readonly translateService: TranslateService,
    private readonly accountService: AccountService
  ) {
    this.accountService.getObservableAccount().pipe(
      first(),
      retry({count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS}),
    ).subscribe({
      next: account => {
        if (!account) {
          return;
        }

        this.initForm(account);

        this.currentAccount = account;
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  private initForm(account: Account) {
    this.preferencesForm.patchValue({
      language: account.preferences.language
    });

    this.preferencesForm.disable();
  }

  protected onEditClick() {
    this.editing = true;

    this.preferencesForm.enable();
  }

  protected onCancelClick() {
    this.editing = false;

    this.initForm(this.currentAccount!);
  }

  protected onSaveClick() {
    const formValues = this.preferencesForm.getRawValue();
    let preferences: AccountPreferencesUpdate = {
      language: formValues.language!
    };

    this.accountService.updateCurrentAccountPreferences(preferences).subscribe({
      next: () => {
        this.messageService.add({
          severity: "success",
          summary: this.translateService.instant("core.pages.account-detail.edit-preferences-message")
        });

        globalThis.location.reload();
      },
      error: () => {
      }
    });
  }
}
