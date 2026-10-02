import {Component, signal} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {ConfirmDialog} from 'primeng/confirmdialog';
import {Toast, ToastPassThroughOptions} from 'primeng/toast';
import {ConfirmationService} from 'primeng/api';
import {DialogService} from 'primeng/dynamicdialog';
import {TranslatePipe} from '@ngx-translate/core';
import {NgClass} from '@angular/common';
import {TopbarComponent} from 'app/core/components/topbar/topbar.component';
import {SidebarComponent} from 'app/core/components/sidebar/sidebar.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, TopbarComponent, SidebarComponent, ConfirmDialog, Toast, TranslatePipe, NgClass],
  providers: [DialogService, ConfirmationService],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('modularhub-fe');

  protected toastPT: ToastPassThroughOptions = {
    messageContent: {
      class: "justify-between!"
    }
  };

}
