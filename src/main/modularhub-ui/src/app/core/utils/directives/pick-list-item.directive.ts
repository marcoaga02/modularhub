import {Directive, Input} from '@angular/core';

interface PickListItemTemplateContext<TItem> {
  $implicit: TItem;
  selected: boolean;
}

@Directive({
  selector: '[appPickListItem]',
})
export class PickListItemDirective<TItem> {
  @Input('appPickListItem') items!: TItem[];

  static ngTemplateContextGuard<TContextItem>(
    dir: PickListItemDirective<TContextItem>,
    ctx: unknown
  ): ctx is PickListItemTemplateContext<TContextItem> {
    return true;
  }
}
