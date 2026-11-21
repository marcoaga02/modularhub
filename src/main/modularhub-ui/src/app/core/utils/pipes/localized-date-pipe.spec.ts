import { TestBed } from '@angular/core/testing';
import { formatDate, registerLocaleData } from '@angular/common';
import localeIt from '@angular/common/locales/it';
import { provideTranslateService, TranslateService } from '@ngx-translate/core';
import { LocalizedDatePipe } from 'app/core/utils/pipes/localized-date-pipe';

describe('LocalizedDatePipe', () => {
  const date = new Date(2026, 0, 31, 14, 5);

  let translateService: TranslateService;
  let pipe: LocalizedDatePipe;

  beforeAll(() => {
    registerLocaleData(localeIt, 'it');
  });

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideTranslateService()],
    });
    translateService = TestBed.inject(TranslateService);
    pipe = new LocalizedDatePipe(translateService);
  });

  it('should format the date with the locale of the current language', () => {
    translateService.use('it');

    expect(pipe.transform(date, 'dd MMMM yyyy, HH:mm')).toBe('31 gennaio 2026, 14:05');
  });

  it('should follow the language when it changes', () => {
    translateService.use('en');

    expect(pipe.transform(date, 'dd MMMM yyyy, HH:mm')).toBe('31 January 2026, 14:05');
  });

  it('should use the medium format by default', () => {
    translateService.use('en');

    expect(pipe.transform(date)).toBe(formatDate(date, 'medium', 'en'));
  });
});
