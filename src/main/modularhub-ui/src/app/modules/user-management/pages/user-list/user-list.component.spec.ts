import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UserListComponent} from 'app/modules/user-management/pages/user-list/user-list.component';
import {DialogService} from 'primeng/dynamicdialog';
import {TranslateService} from '@ngx-translate/core';
import {ConfirmationService, MessageService} from 'primeng/api';
import {OAuthService} from 'angular-oauth2-oidc';

describe('UserListComponent', () => {
  let component: UserListComponent;
  let fixture: ComponentFixture<UserListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserListComponent],
      providers: [
        { provide: DialogService, useValue: {} },
        { provide: TranslateService, useValue: {} },
        { provide: MessageService, useValue: {} },
        { provide: ConfirmationService, useValue: {} },
        { provide: OAuthService, useValue: {} },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
