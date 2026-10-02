import {ComponentFixture, TestBed} from '@angular/core/testing';
import {
  AccountDetailHeaderComponent
} from 'app/core/pages/account-detail/account-detail-header/account-detail-header.component';
import {TranslateService} from '@ngx-translate/core';
import {OAuthService} from 'angular-oauth2-oidc';


describe('AccountDetailHeaderComponent', () => {
  let component: AccountDetailHeaderComponent;
  let fixture: ComponentFixture<AccountDetailHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountDetailHeaderComponent],
      providers: [
        { provide: TranslateService, useValue: {} },
        { provide: OAuthService, useValue: {} },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AccountDetailHeaderComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
