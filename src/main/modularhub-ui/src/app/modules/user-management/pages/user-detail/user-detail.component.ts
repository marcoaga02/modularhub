import { Component, computed, inject, input, output } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { catchError, map, Observable, of, retry, startWith, switchMap } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { Avatar } from 'primeng/avatar';
import { Tag } from 'primeng/tag';
import { Button } from 'primeng/button';
import { Tooltip } from 'primeng/tooltip';
import { Audit } from 'app/core/api/generated';
import { AccountService } from 'app/core/api/services/account.service';
import { SectionTitleComponent } from 'app/core/components/section-title/section-title.component';
import { MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS } from 'app/core/utils/constants/retry';
import { LocalizedDatePipe } from 'app/core/utils/pipes/localized-date-pipe';
import { getFullName, getInitials } from 'app/core/utils/functions/person-name';
import { UUID } from 'app/core/api/models/uuid';
import { UserResponse } from 'app/modules/user-management/api/generated';
import { UserService } from 'app/modules/user-management/api/services/user.service';

@Component({
  selector: 'app-user-detail',
  imports: [TranslatePipe, Avatar, Tag, Button, Tooltip, SectionTitleComponent, LocalizedDatePipe],
  templateUrl: './user-detail.component.html',
})
export class UserDetailComponent {
  private readonly userService = inject(UserService);
  private readonly accountService = inject(AccountService);

  readonly user = input.required<UserResponse>();
  readonly edit = output<UserResponse>();
  readonly remove = output<UserResponse>();
  readonly dismiss = output<void>();

  protected readonly fullName = computed(() =>
    getFullName(this.user().firstname, this.user().lastname),
  );
  protected readonly initials = computed(() =>
    getInitials(this.user().firstname, this.user().lastname),
  );
  protected readonly isCurrentAccount = computed(
    () => this.user().username === this.accountService.account()?.username,
  );

  protected readonly audit = toSignal(
    toObservable(this.user).pipe(switchMap((user) => this.loadAudit(user.id))),
    { initialValue: undefined },
  );

  private loadAudit(id: UUID): Observable<Audit | undefined> {
    return this.userService.getUserById(id).pipe(
      retry({ count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS }),
      map((user) => user.audit),
      catchError(() => of<Audit | undefined>(undefined)),
      startWith<Audit | undefined>(undefined),
    );
  }
}
