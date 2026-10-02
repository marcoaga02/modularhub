import {Component, inject, signal} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {UUID} from 'app/core/api/models/uuid';
import {AccountService} from 'app/core/api/services/account.service';
import {Account} from 'app/core/api/models/account';
import {first, forkJoin, Observable, retry} from 'rxjs';
import {MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS} from 'app/app.config';
import {Select} from 'primeng/select';
import {TranslatePipe} from '@ngx-translate/core';
import {LanguageService} from 'app/modules/user-management/api/services/language.service';
import {Language} from 'app/core/api/models/language';
import {Button} from 'primeng/button';

type LanguageWithValue = {
  label: string,
  value: string
}

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
    language: this.formBuilder.nonNullable.control<UUID | null>(null, Validators.required),
  });

  protected loading = signal(true);
  protected editing = false;

  protected languages: LanguageWithValue[] = [];

  constructor(
    private readonly accountService: AccountService,
    private readonly languageService: LanguageService
  ) {
    forkJoin([
      this.languageService.getLanguages().pipe(
        retry({count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS}),
      ) as Observable<Language[]>,
      this.accountService.getObservableAccount().pipe(
        first(),
        retry({count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS}),
      ) as Observable<Account | null>
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

        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  private initForm(account: Account) {
    this.preferencesForm.patchValue({
      language: account.preferences.language!.id as UUID,
    });

    this.preferencesForm.disable();
  }

  protected onCancel() {

  }

  protected onSave() {

  }
}
