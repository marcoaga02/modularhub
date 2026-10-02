import {ComponentFixture, TestBed} from '@angular/core/testing';

import {UserFormFooterComponent} from './user-form-footer.component';
import {BehaviorSubject, Subject} from 'rxjs';
import {DynamicDialogConfig} from 'primeng/dynamicdialog';
import {ConfirmationService} from 'primeng/api';
import {TranslateService} from '@ngx-translate/core';

describe('UserFormFooterComponent', () => {
  let component: UserFormFooterComponent;
  let fixture: ComponentFixture<UserFormFooterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserFormFooterComponent],
      providers: [
        {
          provide: DynamicDialogConfig, useValue: {
            data: {
              formValid: new BehaviorSubject(false),
              buttonClick: new Subject()
            }
          }
        },
        { provide: ConfirmationService, useValue: {} },
        { provide: TranslateService, useValue: {} },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserFormFooterComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
