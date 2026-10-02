import {Component, effect, input, signal} from '@angular/core';
import {NgClass, TitleCasePipe, UpperCasePipe} from '@angular/common';
import {TranslatePipe} from '@ngx-translate/core';
import {User} from 'app/modules/user-management/api/models/user';
import {UserService} from 'app/modules/user-management/api/services/user.service';
import {retry} from 'rxjs';
import {MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS} from 'app/app.config';
import {ProgressSpinner} from 'primeng/progressspinner';
import {Avatar, AvatarPassThroughOptions} from 'primeng/avatar';
import {Tag} from 'primeng/tag';
import {LocalizedDatePipe} from 'app/core/utils/pipes/localized-date-pipe';

@Component({
  selector: 'app-user-detail',
  imports: [
    NgClass,
    TranslatePipe,
    ProgressSpinner,
    Avatar,
    Tag,
    UpperCasePipe,
    TitleCasePipe,
    LocalizedDatePipe
  ],
  templateUrl: './user-detail.component.html',
  styleUrl: './user-detail.component.css',
})
export class UserDetailComponent {
  readonly isOpen = input(false);
  readonly userId = input<string>();

  protected readonly user = signal<User | null>(null);
  protected readonly loading = signal(false);
  protected avatarPT: AvatarPassThroughOptions = {
    label: {
      class: "text-base!"
    }
  };

  constructor(
    private readonly userService: UserService
  ) {
    effect(() => {
      const id = this.userId();

      if (id) {
        this.loading.set(true);
        this.userService.getUserById(id)
          .pipe(retry({count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS}))
          .subscribe({
            next: user => {
              this.user.set(user);
              this.loading.set(false);
            },
            error: () => {
              this.user.set(null);
              this.loading.set(false);
            }
          });
      } else {
        this.user.set(null);
      }
    });
  }

  protected getFullName(user: User) {
    return `${user.firstname} ${user.lastname}`;
  }

  protected getInitials(user: User): string {
    return `${user.firstname?.[0] ?? ''}${user.lastname?.[0] ?? ''}`.toUpperCase();
  }
}
