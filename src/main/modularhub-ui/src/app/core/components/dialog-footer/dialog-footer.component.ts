import {Component} from '@angular/core';
import {Button} from 'primeng/button';
import {AsyncPipe} from '@angular/common';
import {TranslatePipe} from '@ngx-translate/core';
import {BehaviorSubject, Subject} from 'rxjs';
import {DynamicDialogConfig} from 'primeng/dynamicdialog';

export type DialogFooterConfig = {
  formValid: BehaviorSubject<boolean>;
  formSubmit: Subject<void>;
}

@Component({
  selector: 'app-dialog-footer',
  imports: [
    Button,
    AsyncPipe,
    TranslatePipe
  ],
  templateUrl: './dialog-footer.component.html',
  styleUrl: './dialog-footer.component.css',
})
export class DialogFooterComponent {
  protected formValid: BehaviorSubject<boolean>;

  constructor(private readonly config: DynamicDialogConfig<DialogFooterConfig>) {
    this.formValid = this.config.data!.formValid;
  }

  protected onConfirmButtonClick() {
    this.config.data!.formSubmit.next();
  }
}
