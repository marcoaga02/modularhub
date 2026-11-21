import { Component, inject } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { Button } from 'primeng/button';
import { AsyncPipe } from '@angular/common';
import { BehaviorSubject, Subject } from 'rxjs';
import { DynamicDialogConfig } from 'primeng/dynamicdialog';

export type UserFormFooterConfig = {
  formValid: BehaviorSubject<boolean>;
  buttonClick: Subject<'confirm' | 'cancel' | 'roles'>;
};

@Component({
  selector: 'app-user-form-footer',
  imports: [TranslatePipe, Button, AsyncPipe],
  templateUrl: './user-form-footer.component.html',
  styleUrl: './user-form-footer.component.css',
})
export class UserFormFooterComponent {
  private readonly config = inject<DynamicDialogConfig<UserFormFooterConfig>>(DynamicDialogConfig);
  protected readonly formValid = this.config.data!.formValid;

  protected onRolesButtonClick() {
    this.config.data!.buttonClick.next('roles');
  }

  protected onCancelButtonClick() {
    this.config.data!.buttonClick.next('cancel');
  }

  protected onConfirmButtonClick() {
    this.config.data!.buttonClick.next('confirm');
  }
}
