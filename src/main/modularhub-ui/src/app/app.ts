import { afterNextRender, Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { Toast, ToastPassThroughOptions } from 'primeng/toast';
import { DialogService } from 'primeng/dynamicdialog';
import { TranslatePipe } from '@ngx-translate/core';
import { Location, NgClass } from '@angular/common';
import { MessageService } from 'primeng/api';
import { TopbarComponent } from 'app/core/components/topbar/topbar.component';
import { SidebarComponent } from 'app/core/components/sidebar/sidebar.component';
import { takePasswordUpdateStatus } from 'app/core/auth/password-update';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    TopbarComponent,
    SidebarComponent,
    ConfirmDialog,
    Toast,
    TranslatePipe,
    NgClass,
  ],
  providers: [DialogService],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly messageService = inject(MessageService);
  private readonly passwordUpdated = takePasswordUpdateStatus(inject(Location)) === 'success';

  protected readonly title = signal('modularhub-fe');

  protected toastPT: ToastPassThroughOptions = {
    messageContent: {
      class: 'justify-between',
    },
  };

  constructor() {
    afterNextRender(() => {
      if (this.passwordUpdated) {
        this.messageService.add({ severity: 'success', detail: 'core.auth.password-updated' });
      }
    });
  }
}
