import {ComponentFixture, TestBed} from '@angular/core/testing';

import {UserFormComponent} from './user-form.component';
import {Subject} from 'rxjs';
import {DialogService, DynamicDialogConfig, DynamicDialogRef} from 'primeng/dynamicdialog';
import {ConfirmationService, MessageService} from 'primeng/api';
import {TranslateService} from '@ngx-translate/core';

describe('UserFormComponent', () => {
  let component: UserFormComponent;
  let fixture: ComponentFixture<UserFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserFormComponent],
      providers: [
        {
          provide: DynamicDialogConfig, useValue: {
            data: {
              formSubmit: new Subject(),
              buttonClick: new Subject(),
              userId: undefined
            }
          }
        },
        {
          provide: DynamicDialogRef, useValue: {
            close: () => {
            }
          }
        },
        {provide: ConfirmationService, useValue: {}},
        {provide: TranslateService, useValue: {}},
        {provide: DialogService, useValue: {}},
        {provide: MessageService, useValue: {}},
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
