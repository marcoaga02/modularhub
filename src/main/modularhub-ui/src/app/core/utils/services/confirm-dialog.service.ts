import { inject, Injectable } from '@angular/core';
import { ConfirmationService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root',
})
export class ConfirmDialogService {
  private readonly confirmationService = inject(ConfirmationService);
  private readonly translateService = inject(TranslateService);

  confirm(headerKey: string, messageKey: string, accept: () => void): void {
    this.confirmationService.confirm({
      header: this.translateService.instant(headerKey),
      message: this.translateService.instant(messageKey),
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
      },
      accept,
    });
  }
}
