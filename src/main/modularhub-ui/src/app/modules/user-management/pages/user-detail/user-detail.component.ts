import { Component, computed, inject, input } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { NgClass, UpperCasePipe } from '@angular/common';
import { catchError, map, Observable, of, retry, startWith, switchMap } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Avatar, AvatarPassThroughOptions } from 'primeng/avatar';
import { Tag } from 'primeng/tag';
import { UserResponse } from 'app/modules/user-management/api/generated';
import { UserService } from 'app/modules/user-management/api/services/user.service';
import { MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS } from 'app/core/utils/constants/retry';
import { LocalizedDatePipe } from 'app/core/utils/pipes/localized-date-pipe';
import { getFullName, getInitials } from 'app/core/utils/functions/person-name';
import { UUID } from 'app/core/api/models/uuid';

type UserState = {
  user: UserResponse | null;
  loading: boolean;
};

const NO_USER: UserState = { user: null, loading: false };
const LOADING_USER: UserState = { user: null, loading: true };

@Component({
  selector: 'app-user-detail',
  imports: [NgClass, TranslatePipe, ProgressSpinner, Avatar, Tag, UpperCasePipe, LocalizedDatePipe],
  templateUrl: './user-detail.component.html',
  styleUrl: './user-detail.component.css',
})
export class UserDetailComponent {
  private readonly userService = inject(UserService);

  readonly isOpen = input(false);
  readonly userId = input<UUID>();

  protected readonly avatarPT: AvatarPassThroughOptions = {
    label: {
      class: 'text-base!',
    },
  };

  private readonly state = toSignal(
    toObservable(this.userId).pipe(switchMap((id) => (id ? this.loadUser(id) : of(NO_USER)))),
    { initialValue: NO_USER },
  );

  protected readonly user = computed(() => this.state().user);
  protected readonly loading = computed(() => this.state().loading);
  protected readonly initials = computed(() => {
    const user = this.user();
    return user ? getInitials(user.firstname, user.lastname) : '';
  });
  protected readonly fullName = computed(() => {
    const user = this.user();
    return user ? getFullName(user.firstname, user.lastname) : '';
  });

  private loadUser(id: UUID): Observable<UserState> {
    return this.userService.getUserById(id).pipe(
      retry({ count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS }),
      map((user) => ({ user, loading: false })),
      catchError(() => of(NO_USER)),
      startWith(LOADING_USER),
    );
  }
}
