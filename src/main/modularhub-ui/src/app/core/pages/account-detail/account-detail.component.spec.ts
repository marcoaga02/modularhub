import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal, WritableSignal } from '@angular/core';
import { of, Subject } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { DynamicDialogRef } from 'primeng/dynamicdialog';
import { AccountDetailComponent } from 'app/core/pages/account-detail/account-detail.component';
import { AccountService } from 'app/core/api/services/account.service';
import { Account, AccountPreferencesResponse, Gender, Language } from 'app/core/api/generated';

describe('AccountDetailComponent', () => {
  const account: Account = {
    identityId: 'identity-id',
    email: 'mario@test.it',
    username: 'mario.rossi',
    firstName: 'Mario',
    lastName: 'Rossi',
    gender: Gender.M,
    taxIdNumber: 'RSSMRA80A01H501U',
    mobileNumber: '3331234567',
    permissions: [],
    preferences: { language: Language.It },
  };

  let fixture: ComponentFixture<AccountDetailComponent>;
  let element: HTMLElement;
  let accountSignal: WritableSignal<Account | null>;
  let accountService: {
    account: WritableSignal<Account | null>;
    updateCurrentAccountPreferences: ReturnType<typeof vi.fn>;
  };
  let closed: number;

  beforeEach(async () => {
    accountSignal = signal<Account | null>(account);
    accountService = {
      account: accountSignal,
      updateCurrentAccountPreferences: vi.fn().mockReturnValue(of({ language: Language.En })),
    };
    const ref = new DynamicDialogRef();
    closed = 0;
    ref.onClose.subscribe(() => closed++);

    await TestBed.configureTestingModule({
      imports: [AccountDetailComponent],
      providers: [
        provideTranslateService(),
        { provide: AccountService, useValue: accountService },
        { provide: DynamicDialogRef, useValue: ref },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AccountDetailComponent);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  function selectedLanguage(): string {
    return element.querySelector('p-select')!.textContent!.trim();
  }

  async function chooseLanguage(language: Language) {
    element.querySelector<HTMLElement>('p-select')!.click();
    await fixture.whenStable();
    Array.from(document.querySelectorAll<HTMLElement>('li[role="option"]'))
      .find((option) => option.textContent?.includes(`model.language.${language}`))!
      .click();
    await fixture.whenStable();
  }

  it('should show the data of the account', () => {
    const text = element.textContent;
    expect(text).toContain('Mario Rossi');
    expect(text).toContain('model.gender.male');
    expect(text).toContain('RSSMRA80A01H501U');
    expect(text).toContain('3331234567');
    expect(text).toContain('mario@test.it');
    expect(text).toContain('mario.rossi');
    expect(selectedLanguage()).toBe('model.language.it');
  });

  it('should hide the optional data the account does not have', async () => {
    accountSignal.set({
      ...account,
      gender: undefined,
      taxIdNumber: undefined,
      mobileNumber: undefined,
    });
    await fixture.whenStable();

    const text = element.textContent;
    expect(text).not.toContain('model.account.gender');
    expect(text).not.toContain('model.account.tax-id-number');
    expect(text).not.toContain('model.account.mobile-number');
  });

  it('should close the dialog from the close button', async () => {
    element
      .querySelector<HTMLButtonElement>('header button[aria-label="general.button.label.close"]')!
      .click();
    await fixture.whenStable();

    expect(closed).toBe(1);
  });

  it('should save the language as soon as it is chosen', async () => {
    await chooseLanguage(Language.En);

    expect(accountService.updateCurrentAccountPreferences).toHaveBeenCalledWith({
      language: Language.En,
    });
    expect(selectedLanguage()).toBe('model.language.en');
  });

  it('should disable the language while it is being saved', async () => {
    const response = new Subject<AccountPreferencesResponse>();
    accountService.updateCurrentAccountPreferences.mockReturnValue(response);

    await chooseLanguage(Language.En);

    expect(element.querySelector('p-select')!.classList).toContain('p-disabled');

    response.next({ language: Language.En });
    response.complete();
    await fixture.whenStable();

    expect(element.querySelector('p-select')!.classList).not.toContain('p-disabled');
  });

  it('should go back to the previous language when the save fails', async () => {
    const response = new Subject<AccountPreferencesResponse>();
    accountService.updateCurrentAccountPreferences.mockReturnValue(response);
    await chooseLanguage(Language.En);

    response.error(new Error('unavailable'));
    await fixture.whenStable();

    expect(selectedLanguage()).toBe('model.language.it');
    expect(element.querySelector('p-select')!.classList).not.toContain('p-disabled');
  });
});
