import { TestBed } from '@angular/core/testing';
import { Confirmation, ConfirmationService } from 'primeng/api';
import { provideTranslateService } from '@ngx-translate/core';
import { ConfirmDialogService } from 'app/core/utils/services/confirm-dialog.service';

describe('ConfirmDialogService', () => {
  let service: ConfirmDialogService;
  let confirmations: Confirmation[];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideTranslateService(), ConfirmationService],
    });
    confirmations = [];
    TestBed.inject(ConfirmationService).requireConfirmation$.subscribe(
      (c) => c && confirmations.push(c),
    );
    service = TestBed.inject(ConfirmDialogService);
  });

  it('should ask a confirmation with the translated header, message and buttons', () => {
    service.confirm('dialog.title', 'dialog.message', () => undefined);

    expect(confirmations).toHaveLength(1);
    expect(confirmations[0]).toEqual(
      expect.objectContaining({
        header: 'dialog.title',
        message: 'dialog.message',
        closable: true,
        closeOnEscape: true,
        icon: 'pi pi-exclamation-triangle',
        rejectButtonProps: {
          label: 'general.button.label.cancel',
          severity: 'secondary',
          outlined: true,
        },
        acceptButtonProps: { label: 'general.button.label.confirm' },
      }),
    );
  });

  it('should run the action only when the confirmation is accepted', () => {
    const accept = vi.fn();

    service.confirm('dialog.title', 'dialog.message', accept);
    expect(accept).not.toHaveBeenCalled();

    confirmations[0].accept!();

    expect(accept).toHaveBeenCalledOnce();
  });
});
