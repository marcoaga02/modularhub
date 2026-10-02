import {Pipe, PipeTransform} from '@angular/core';
import {DatePipe} from '@angular/common';
import {TranslateService} from '@ngx-translate/core';

@Pipe({
  name: 'localizedDate',
})
export class LocalizedDatePipe implements PipeTransform {
  constructor(
    private readonly translateService: TranslateService
  ) {
  }

  transform(value: string | number | Date, format = 'medium'): string | null {
    const currentLang = this.translateService.getCurrentLang();

    const datePipe = new DatePipe(currentLang);
    return datePipe.transform(value, format);
  }
}
