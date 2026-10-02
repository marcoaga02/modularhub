import {LocalizedDatePipe} from 'app/core/utils/pipes/localized-date-pipe';
import {TranslateService} from '@ngx-translate/core';

describe('LocalizedDatePipe', () => {
  it('create an instance', () => {
    const mockTranslateService = { getCurrentLang: () => 'it' };
    const pipe = new LocalizedDatePipe(mockTranslateService as TranslateService);
    expect(pipe).toBeTruthy();
  });
});
