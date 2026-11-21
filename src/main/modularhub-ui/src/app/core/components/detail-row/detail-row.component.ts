import { Component, input } from '@angular/core';

@Component({
  selector: 'div[app-detail-row]',
  templateUrl: './detail-row.component.html',
  host: { class: 'grid grid-cols-[7rem_1fr] gap-3 py-1.5' },
})
export class DetailRowComponent {
  readonly label = input.required<string>();
}
