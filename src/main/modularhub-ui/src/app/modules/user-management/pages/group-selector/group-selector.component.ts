import {Component} from '@angular/core';
import {PickList, PickListPassThroughOptions} from 'primeng/picklist';
import {DynamicDialogConfig, DynamicDialogRef} from 'primeng/dynamicdialog';
import {DialogFooterConfig} from 'app/core/components/dialog-footer/dialog-footer.component';
import {Group} from 'app/core/api/models/group';
import {PickListItemDirective} from 'app/core/utils/directives/pick-list-item.directive';
import {PrimeTemplate} from 'primeng/api';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {TranslatePipe} from '@ngx-translate/core';
import {UUID} from 'app/core/api/models/uuid';

@Component({
  selector: 'app-group-selector',
  imports: [
    PickList,
    PickListItemDirective,
    PrimeTemplate,
    TranslatePipe
  ],
  templateUrl: './group-selector.component.html',
  styleUrl: './group-selector.component.css',
})
export class GroupSelectorComponent {

  protected pickListPT: PickListPassThroughOptions = {
    sourceControls: {
      class: "hidden!"
    },
    targetControls: {
      class: "hidden!"
    },
    sourceListContainer: {
      class: "overflow-hidden!"
    },
    targetListContainer: {
      class: "overflow-hidden!"
    }
  };

  protected availableGroups: Group[] = [];
  protected selectedGroups: Group[] = [];

  constructor(
    private readonly config: DynamicDialogConfig<DialogFooterConfig & {
      groups: Group[],
      selectedGroupIds: UUID[],
    }>,
    private readonly ref: DynamicDialogRef
  ) {
    this.config.data!.formSubmit
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.submit())

    this.selectedGroups = this.config.data!.groups.filter(
      g => this.config.data?.selectedGroupIds.includes(g.id)
    );
    this.availableGroups = this.config.data!.groups.filter(
      g => !this.config.data?.selectedGroupIds.includes(g.id)
    );
  }

  private submit() {
    this.ref.close(
      this.selectedGroups.map(g => g.id)
    );
  }
}
