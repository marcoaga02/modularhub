import { Component, computed, inject, linkedSignal, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { Avatar } from 'primeng/avatar';
import { Button } from 'primeng/button';
import { Select } from 'primeng/select';
import { DynamicDialogRef } from 'primeng/dynamicdialog';
import { AccountService } from 'app/core/api/services/account.service';
import { Language } from 'app/core/api/generated';
import { LANGUAGES } from 'app/core/utils/constants/languages';
import { getFullName, getInitials } from 'app/core/utils/functions/person-name';
import { SectionTitleComponent } from 'app/core/components/section-title/section-title.component';
import { DetailRowComponent } from 'app/core/components/detail-row/detail-row.component';

@Component({
  selector: 'app-account-detail',
  imports: [
    FormsModule,
    TranslatePipe,
    Avatar,
    Button,
    Select,
    SectionTitleComponent,
    DetailRowComponent,
  ],
  templateUrl: './account-detail.component.html',
  host: { class: 'flex min-h-0 flex-1 flex-col' },
})
export class AccountDetailComponent {
  private readonly accountService = inject(AccountService);
  protected readonly dialogRef = inject(DynamicDialogRef);

  protected readonly languages = LANGUAGES;
  protected readonly account = this.accountService.account;
  protected readonly initials = computed(() => {
    const account = this.account();
    return account ? getInitials(account.firstName, account.lastName) : '';
  });
  protected readonly fullName = computed(() => {
    const account = this.account();
    return account ? getFullName(account.firstName, account.lastName) : '';
  });
  protected readonly language = linkedSignal(() => this.account()?.preferences.language);
  protected readonly saving = signal(false);

  protected onLanguageChange(language: Language) {
    this.language.set(language);
    this.saving.set(true);

    this.accountService.updateCurrentAccountPreferences({ language }).subscribe({
      next: () => this.saving.set(false),
      error: () => {
        this.language.set(this.account()?.preferences.language);
        this.saving.set(false);
      },
    });
  }

  protected changePassword() {
    this.accountService.changePassword();
  }
}
