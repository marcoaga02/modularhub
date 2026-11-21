import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject } from 'rxjs';
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
  let accountSubject: BehaviorSubject<Account | null>;

  beforeEach(async () => {
    accountSubject = new BehaviorSubject<Account | null>(null);

    await TestBed.configureTestingModule({
      imports: [AccountDetailHeaderComponent],
      providers: [
        {
          provide: AccountService,
          useValue: { getObservableAccount: () => accountSubject.asObservable() },
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
    accountSubject.next(account);
    await fixture.whenStable();

    expect(element.querySelector('p-avatar')!.textContent?.trim()).toBe('MR');
    expect(element.textContent).toContain('mario rossi');
    expect(element.textContent).toContain('mario.rossi');
  });

  it('should stop listening to the account when it is destroyed', async () => {
    fixture.destroy();

    expect(accountSubject.observed).toBe(false);
  });
});
