import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TopbarComponent} from 'app/core/components/topbar/topbar.component';
import {TranslateService} from '@ngx-translate/core';
import {OAuthService} from 'angular-oauth2-oidc';
import {ConfirmationService} from 'primeng/api';
import {DialogService} from 'primeng/dynamicdialog';

describe('TopbarComponent', () => {
  let component: TopbarComponent;
  let fixture: ComponentFixture<TopbarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TopbarComponent],
      providers: [
        { provide: TranslateService, useValue: {} },
        { provide: OAuthService, useValue: {} },
        { provide: ConfirmationService, useValue: {} },
        { provide: DialogService, useValue: {} },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TopbarComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
