import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { PluginService } from 'app/core/api/services/plugin.service';
import { AuthService } from 'app/core/auth/auth.service';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, TranslatePipe],
  templateUrl: './sidebar.component.html',
  host: {
    class: 'relative z-20 shrink-0 transition-[width] duration-300',
    '[class.w-64]': 'isPinned()',
    '[class.w-16]': '!isPinned()',
  },
})
export class SidebarComponent {
  private readonly authService = inject(AuthService);

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
    this.authService.logout();
  }
}
