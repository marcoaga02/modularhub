import {Component} from '@angular/core';
import {TranslatePipe} from '@ngx-translate/core';
import {Button} from 'primeng/button';
import {AsyncPipe} from '@angular/common';
import {BehaviorSubject, Subject} from 'rxjs';
import {DynamicDialogConfig} from 'primeng/dynamicdialog';

export type UserFormFooterConfig = {
  formValid: BehaviorSubject<boolean>;
  buttonClick: Subject<'confirm' | 'cancel' | 'groups'>;
}

@Component({
  selector: 'app-user-form-footer',
  imports: [
    TranslatePipe,
    Button,
    AsyncPipe
  ],
  templateUrl: './user-form-footer.component.html',
  styleUrl: './user-form-footer.component.css',
})
export class UserFormFooterComponent {

  protected formValid: BehaviorSubject<boolean>;

  constructor(
    private readonly config: DynamicDialogConfig<UserFormFooterConfig>
  ) {
    this.formValid = this.config.data!.formValid;
  }

  protected onGroupsButtonClick() {
    this.config.data!.buttonClick.next('groups');
  }

  protected onCancelButtonClick() {
    this.config.data!.buttonClick.next('cancel');
  }

  protected onConfirmButtonClick() {
    this.config.data!.buttonClick.next('confirm');
  }
}
