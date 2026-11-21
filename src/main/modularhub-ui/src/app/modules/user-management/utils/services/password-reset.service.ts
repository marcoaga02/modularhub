import { inject, Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { UUID } from 'app/core/api/models/uuid';
import { ConfirmDialogService } from 'app/core/utils/services/confirm-dialog.service';
import { UserService } from 'app/modules/user-management/api/services/user.service';

const RESET_PASSWORD = 'modules.user-management.reset-password';

@Injectable({ providedIn: 'root' })
export class PasswordResetService {
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly userService = inject(UserService);
  private readonly messageService = inject(MessageService);
  private readonly translateService = inject(TranslateService);

  resetPassword(userId: UUID): void {
    this.confirmDialog.confirm(
      `${RESET_PASSWORD}.confirmation-title`,
      `${RESET_PASSWORD}.confirmation-message`,
      () => this.sendResetEmail(userId),
    );
  }

  private sendResetEmail(userId: UUID) {
    this.userService.resetPassword(userId).subscribe({
      next: () =>
        this.messageService.add({
          severity: 'success',
          summary: this.translateService.instant(`${RESET_PASSWORD}.success`),
        }),
      error: () => undefined,
    });
  }
}
