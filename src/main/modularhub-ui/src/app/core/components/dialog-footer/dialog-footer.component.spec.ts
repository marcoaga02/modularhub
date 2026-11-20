import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DialogFooterComponent} from './dialog-footer.component';
import {DynamicDialogConfig} from 'primeng/dynamicdialog';
import {BehaviorSubject, Subject} from 'rxjs';
import {provideTranslateService} from '@ngx-translate/core';

describe('DialogFooterComponent', () => {
  let component: DialogFooterComponent;
  let fixture: ComponentFixture<DialogFooterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DialogFooterComponent],
      providers: [
        {
          provide: DynamicDialogConfig, useValue: {
            data: {
              formValid: new BehaviorSubject(false),
              formSubmit: new Subject(),
            }
          }
        },
        provideTranslateService(),
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DialogFooterComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
