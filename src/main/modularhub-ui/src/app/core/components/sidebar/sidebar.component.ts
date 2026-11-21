import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { Tooltip } from 'primeng/tooltip';
import { PluginService } from 'app/core/api/services/plugin.service';
import { AccountService } from 'app/core/api/services/account.service';
import { ConfirmDialogService } from 'app/core/utils/services/confirm-dialog.service';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, Tooltip, TranslatePipe],
  templateUrl: './sidebar.component.html',
  host: {
    class: 'relative z-20 shrink-0 transition-[width] duration-300',
    '[class.w-64]': 'isPinned()',
    '[class.w-16]': '!isPinned()',
  },
})
export class SidebarComponent {
  private readonly accountService = inject(AccountService);
  private readonly confirmDialog = inject(ConfirmDialogService);

  protected readonly isPinned = signal(true);
  protected readonly isHovered = signal(false);
  protected readonly isExpanded = computed(() => this.isPinned() || this.isHovered());
  protected readonly isFloating = computed(() => this.isHovered() && !this.isPinned());
  protected readonly plugins = inject(PluginService).plugins;

  protected togglePinned() {
    this.isPinned.update((pinned) => !pinned);
    this.isHovered.set(false);
  }

  protected logout() {
    this.confirmDialog.confirm('general.logout.title', 'general.logout.message', () =>
      this.accountService.logout(),
    );
  }
}
