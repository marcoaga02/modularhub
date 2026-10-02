import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AccountDetailComponent} from './account-detail.component';
import {MessageService} from 'primeng/api';
import {TranslateService} from '@ngx-translate/core';
import {OAuthService} from 'angular-oauth2-oidc';

describe('AccountDetailComponent', () => {
  let component: AccountDetailComponent;
  let fixture: ComponentFixture<AccountDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountDetailComponent],
      providers: [
        { provide: MessageService, useValue: {} },
        { provide: TranslateService, useValue: {} },
        { provide: OAuthService, useValue: {} },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AccountDetailComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
