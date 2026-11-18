import {Component, inject, signal} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {AccountService} from 'app/core/api/services/account.service';
import {Account, Language} from 'app/core/api/generated';
import {first, retry} from 'rxjs';
import {MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS} from 'app/app.config';
import {Select} from 'primeng/select';
import {TranslatePipe} from '@ngx-translate/core';
import {Button} from 'primeng/button';
import {LANGUAGES} from 'app/core/utils/constants/languages';

@Component({
  selector: 'app-account-preferences',
  imports: [
    ReactiveFormsModule,
    Select,
    TranslatePipe,
    Button
  ],
  templateUrl: './account-preferences.component.html',
  styleUrl: './account-preferences.component.css',
})
export class AccountPreferencesComponent {

  private readonly formBuilder = inject(FormBuilder);

  protected preferencesForm = this.formBuilder.nonNullable.group({
    language: this.formBuilder.nonNullable.control<Language | null>(null, Validators.required),
  });

  protected loading = signal(true);
  protected editing = false;

  protected readonly languages = LANGUAGES;

  constructor(private readonly accountService: AccountService) {
    this.accountService.getObservableAccount().pipe(
      first(),
      retry({count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS}),
    ).subscribe({
      next: account => {
        if (!account) {
          return;
        }

        this.initForm(account);

        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  private initForm(account: Account) {
    this.preferencesForm.patchValue({
      language: account.preferences.language,
    });

    this.preferencesForm.disable();
  }

  protected onCancel() {

  }

  protected onSave() {

  }
}
