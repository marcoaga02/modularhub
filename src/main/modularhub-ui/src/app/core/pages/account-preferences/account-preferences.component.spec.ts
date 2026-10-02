import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AccountPreferencesComponent} from './account-preferences.component';
import {TranslateService} from '@ngx-translate/core';
import {OAuthService} from 'angular-oauth2-oidc';

describe('AccountPreferencesComponent', () => {
  let component: AccountPreferencesComponent;
  let fixture: ComponentFixture<AccountPreferencesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountPreferencesComponent],
      providers: [
        { provide: TranslateService, useValue: {} },
        { provide: OAuthService, useValue: {} },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AccountPreferencesComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
