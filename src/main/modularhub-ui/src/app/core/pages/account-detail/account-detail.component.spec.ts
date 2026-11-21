import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal, Signal } from '@angular/core';
import { of } from 'rxjs';
import { provideTranslateService } from '@ngx-translate/core';
import { MessageService, ToastMessageOptions } from 'primeng/api';
import { AccountDetailComponent } from 'app/core/pages/account-detail/account-detail.component';
import { AccountService } from 'app/core/api/services/account.service';
import { Account, Gender, Language } from 'app/core/api/generated';

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
  let accountService: {
    account: Signal<Account | null>;
    updateCurrentAccountPreferences: ReturnType<typeof vi.fn>;
  };
  let messages: ToastMessageOptions[];

  async function setUp(initialAccount: Account | null) {
    accountService = {
      account: signal(initialAccount).asReadonly(),
      updateCurrentAccountPreferences: vi.fn().mockReturnValue(of(account)),
    };

    await TestBed.configureTestingModule({
      imports: [AccountDetailComponent],
      providers: [
        provideTranslateService(),
        MessageService,
        { provide: AccountService, useValue: accountService },
      ],
    }).compileComponents();

    messages = [];
    TestBed.inject(MessageService).messageObserver.subscribe((m) =>
      messages.push(...(Array.isArray(m) ? m : [m])),
    );

    fixture = TestBed.createComponent(AccountDetailComponent);
    element = fixture.nativeElement;
    await fixture.whenStable();
  }

  async function clickButton(label: string) {
    const buttons = Array.from(element.querySelectorAll<HTMLButtonElement>('p-button button'));
    buttons.find((b) => b.textContent?.includes(label))!.click();
    await fixture.whenStable();
  }

  function languageSelect(): HTMLElement | null {
    return element.querySelector('p-select');
  }

  it('should keep loading when there is no account', async () => {
    await setUp(null);

    expect(element.querySelector('p-progress-spinner')).not.toBeNull();
  });

  it('should show the data of the account', async () => {
    await setUp(account);

    const text = element.textContent;
    expect(text).toContain('Mario');
    expect(text).toContain('Rossi');
    expect(text).toContain('model.gender.male');
    expect(text).toContain('RSSMRA80A01H501U');
    expect(text).toContain('3331234567');
    expect(text).toContain('mario@test.it');
    expect(text).toContain('mario.rossi');
    expect(text).toContain('model.language.it');
  });

  it('should show the language selector when edit is clicked', async () => {
    await setUp(account);

    await clickButton('general.button.label.edit');

    expect(languageSelect()).not.toBeNull();
    expect(languageSelect()!.textContent).toContain('model.language.it');
  });

  it('should go back to the read only view when cancel is clicked', async () => {
    await setUp(account);
    await clickButton('general.button.label.edit');

    await clickButton('general.button.label.cancel');

    expect(languageSelect()).toBeNull();
    expect(accountService.updateCurrentAccountPreferences).not.toHaveBeenCalled();
  });

  it('should save the selected language and show a confirmation', async () => {
    await setUp(account);
    await clickButton('general.button.label.edit');

    await clickButton('general.button.label.save');

    expect(accountService.updateCurrentAccountPreferences).toHaveBeenCalledWith({
      language: Language.It,
    });
    expect(messages).toEqual([
      { severity: 'success', summary: 'core.pages.account-detail.edit-preferences-message' },
    ]);
  });
});
