import { Component, input } from '@angular/core';

@Component({
  selector: 'app-section-title',
  templateUrl: './section-title.component.html',
  host: {
    class: 'flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-accent',
  },
})
export class SectionTitleComponent {
  readonly icon = input.required<string>();
}
