import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { MessageService, ToastMessageOptions } from 'primeng/api';
import { UUID } from 'app/core/api/models/uuid';
import { ConfirmDialogService } from 'app/core/utils/services/confirm-dialog.service';
import { UserService } from 'app/modules/user-management/api/services/user.service';
import { PasswordResetService } from 'app/modules/user-management/utils/services/password-reset.service';

describe('PasswordResetService', () => {
  const RESET_PASSWORD = 'modules.user-management.reset-password';
  const USER_ID = 'user-id' as UUID;

  let service: PasswordResetService;
  let confirmDialog: { confirm: ReturnType<typeof vi.fn> };
  let userService: { resetPassword: ReturnType<typeof vi.fn> };
  let messages: ToastMessageOptions[];

  beforeEach(() => {
    confirmDialog = { confirm: vi.fn() };
    userService = { resetPassword: vi.fn().mockReturnValue(of(undefined)) };

    TestBed.configureTestingModule({
      providers: [
        provideTranslateService(),
        MessageService,
        { provide: ConfirmDialogService, useValue: confirmDialog },
        { provide: UserService, useValue: userService },
      ],
    });

    messages = [];
    TestBed.inject(MessageService).messageObserver.subscribe((m) =>
      messages.push(...(Array.isArray(m) ? m : [m])),
    );
    service = TestBed.inject(PasswordResetService);
  });

  function acceptConfirmation() {
    confirmDialog.confirm.mock.lastCall![2]();
  }

  it('should ask a confirmation before resetting the password', () => {
    service.resetPassword(USER_ID);

    expect(confirmDialog.confirm).toHaveBeenCalledWith(
      `${RESET_PASSWORD}.confirmation-title`,
      `${RESET_PASSWORD}.confirmation-message`,
      expect.any(Function),
    );
    expect(userService.resetPassword).not.toHaveBeenCalled();
  });

  it('should reset the password and show a message when the confirmation is accepted', () => {
    service.resetPassword(USER_ID);

    acceptConfirmation();

    expect(userService.resetPassword).toHaveBeenCalledWith(USER_ID);
    expect(messages).toEqual([{ severity: 'success', summary: `${RESET_PASSWORD}.success` }]);
  });

  it('should show no message when the reset fails', () => {
    userService.resetPassword.mockReturnValue(throwError(() => new Error('unavailable')));
    service.resetPassword(USER_ID);

    acceptConfirmation();

    expect(messages).toEqual([]);
  });
});
