import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, Subject } from 'rxjs';
import { DynamicDialogConfig } from 'primeng/dynamicdialog';
import { provideTranslateService } from '@ngx-translate/core';
import { DialogFooterComponent } from 'app/core/components/dialog-footer/dialog-footer.component';

describe('DialogFooterComponent', () => {
  let fixture: ComponentFixture<DialogFooterComponent>;
  let confirmButton: () => HTMLButtonElement;
  let formValid: BehaviorSubject<boolean>;
  let submits: number;

  beforeEach(async () => {
    formValid = new BehaviorSubject(false);
    const formSubmit = new Subject<void>();
    submits = 0;
    formSubmit.subscribe(() => submits++);

    await TestBed.configureTestingModule({
      imports: [DialogFooterComponent],
      providers: [
        provideTranslateService(),
        { provide: DynamicDialogConfig, useValue: { data: { formValid, formSubmit } } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DialogFooterComponent);
    confirmButton = () => fixture.nativeElement.querySelector('p-button button');
    await fixture.whenStable();
  });

  it('should disable confirm while the form is invalid', () => {
    expect(confirmButton().disabled).toBe(true);
  });

  it('should submit the form when confirm is clicked on a valid form', async () => {
    formValid.next(true);
    await fixture.whenStable();

    confirmButton().click();

    expect(submits).toBe(1);
  });
});
