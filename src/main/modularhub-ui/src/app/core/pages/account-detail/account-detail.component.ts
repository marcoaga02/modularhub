import {Component, inject, signal} from '@angular/core';
import {AccountService} from 'app/core/api/services/account.service';
import {first, forkJoin, retry} from 'rxjs';
import {MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS} from 'app/app.config';
import {Account} from 'app/core/api/models/account';
import {ProgressSpinner} from 'primeng/progressspinner';
import {UpperCasePipe} from '@angular/common';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {UUID} from 'app/core/api/models/uuid';
import {LanguageService} from 'app/modules/user-management/api/services/language.service';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {Select} from 'primeng/select';
import {Button} from 'primeng/button';
import {AccountPreferencesRequest} from 'app/core/api/models/account-preferences';
import {MessageService} from 'primeng/api';

type LanguageWithValue = {
  label: string,
  value: string
}

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
    language: this.formBuilder.nonNullable.control<UUID | null>(null, Validators.required),
  });

  protected loading = signal(true);
  protected editing: boolean = false;

  protected currentAccount?: Account;
  protected languages: LanguageWithValue[] = [];

  constructor(
    private readonly messageService: MessageService,
    private readonly translateService: TranslateService,
    private readonly accountService: AccountService,
    private readonly languageService: LanguageService
  ) {
    forkJoin([
      this.languageService.getLanguages().pipe(
        retry({count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS}),
      ),
      this.accountService.getObservableAccount().pipe(
        first(),
        retry({count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS}),
      )
    ]).subscribe({
      next: ([languages, account]) => {
        if (!account || languages.length === 0) {
          return;
        }

        this.languages = languages.map(l => ({
            label: l.label,
            value: l.id
          })
        );

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
      language: account.preferences.language.id
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
    let preferences: AccountPreferencesRequest = {
      languageId: formValues.language!
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
