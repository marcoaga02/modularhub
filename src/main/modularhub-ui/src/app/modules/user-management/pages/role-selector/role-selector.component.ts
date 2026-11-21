import { Component, inject } from '@angular/core';
import { PickList, PickListPassThroughOptions } from 'primeng/picklist';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { DialogFooterConfig } from 'app/core/components/dialog-footer/dialog-footer.component';
import { RoleSummary } from 'app/modules/user-management/api/generated';
import { PickListItemDirective } from 'app/core/utils/directives/pick-list-item.directive';
import { PrimeTemplate } from 'primeng/api';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TranslatePipe } from '@ngx-translate/core';
import { UUID } from 'app/core/api/models/uuid';

@Component({
  selector: 'app-role-selector',
  imports: [PickList, PickListItemDirective, PrimeTemplate, TranslatePipe],
  templateUrl: './role-selector.component.html',
  styleUrl: './role-selector.component.css',
})
export class RoleSelectorComponent {
  protected pickListPT: PickListPassThroughOptions = {
    sourceControls: {
      class: 'hidden!',
    },
    targetControls: {
      class: 'hidden!',
    },
    sourceListContainer: {
      class: 'overflow-hidden!',
    },
    targetListContainer: {
      class: 'overflow-hidden!',
    },
  };

  protected availableRoles: RoleSummary[] = [];
  protected selectedRoles: RoleSummary[] = [];

  private readonly config =
    inject<
      DynamicDialogConfig<DialogFooterConfig & { roles: RoleSummary[]; selectedRoleIds: UUID[] }>
    >(DynamicDialogConfig);
  private readonly ref = inject(DynamicDialogRef);

  constructor() {
    this.config.data!.formSubmit.pipe(takeUntilDestroyed()).subscribe(() => this.submit());

    const { roles, selectedRoleIds } = this.config.data!;
    this.selectedRoles = roles.filter((r) => selectedRoleIds.includes(r.id));
    this.availableRoles = roles.filter((r) => !selectedRoleIds.includes(r.id));
  }

  private submit() {
    this.ref.close(this.selectedRoles.map((r) => r.id));
  }
}
