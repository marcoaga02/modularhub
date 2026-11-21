import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal, WritableSignal } from '@angular/core';
import { AccountDetailHeaderComponent } from 'app/core/pages/account-detail/account-detail-header/account-detail-header.component';
import { AccountService } from 'app/core/api/services/account.service';
import { Account, Language } from 'app/core/api/generated';

describe('AccountDetailHeaderComponent', () => {
  const account: Account = {
    identityId: 'identity-id',
    email: 'mario@test.it',
    username: 'mario.rossi',
    firstName: 'mario',
    lastName: 'rossi',
    permissions: [],
    preferences: { language: Language.It },
  };

  let fixture: ComponentFixture<AccountDetailHeaderComponent>;
  let element: HTMLElement;
  let accountSignal: WritableSignal<Account | null>;

  beforeEach(async () => {
    accountSignal = signal<Account | null>(null);

    await TestBed.configureTestingModule({
      imports: [AccountDetailHeaderComponent],
      providers: [
        {
          provide: AccountService,
          useValue: { account: accountSignal.asReadonly() },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AccountDetailHeaderComponent);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  it('should show nothing until the account is loaded', () => {
    expect(element.textContent?.trim()).toBe('');
  });

  it('should show initials, full name and username of the account', async () => {
    accountSignal.set(account);
    await fixture.whenStable();

    expect(element.querySelector('p-avatar')!.textContent?.trim()).toBe('MR');
    expect(element.textContent).toContain('mario rossi');
    expect(element.textContent).toContain('mario.rossi');
  });
});
