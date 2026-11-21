import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, Subject } from 'rxjs';
import { DynamicDialogConfig } from 'primeng/dynamicdialog';
import { provideTranslateService } from '@ngx-translate/core';
import { UserFormFooterComponent } from 'app/modules/user-management/pages/user-form/user-form-footer/user-form-footer.component';

describe('UserFormFooterComponent', () => {
  let fixture: ComponentFixture<UserFormFooterComponent>;
  let element: HTMLElement;
  let formValid: BehaviorSubject<boolean>;
  let clicks: string[];

  beforeEach(async () => {
    formValid = new BehaviorSubject(false);
    const buttonClick = new Subject<'confirm' | 'cancel' | 'roles'>();
    clicks = [];
    buttonClick.subscribe((action) => clicks.push(action));

    await TestBed.configureTestingModule({
      imports: [UserFormFooterComponent],
      providers: [
        provideTranslateService(),
        { provide: DynamicDialogConfig, useValue: { data: { formValid, buttonClick } } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UserFormFooterComponent);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  function button(label: string): HTMLButtonElement {
    return Array.from(element.querySelectorAll<HTMLButtonElement>('p-button button')).find((b) =>
      b.textContent?.includes(label),
    )!;
  }

  it('should disable confirm while the form is invalid', () => {
    expect(button('general.button.label.confirm').disabled).toBe(true);
  });

  it('should enable confirm when the form becomes valid', async () => {
    formValid.next(true);
    await fixture.whenStable();

    expect(button('general.button.label.confirm').disabled).toBe(false);
  });

  it('should notify which button is clicked', async () => {
    formValid.next(true);
    await fixture.whenStable();

    button('model.user.roles').click();
    button('general.button.label.cancel').click();
    button('general.button.label.confirm').click();

    expect(clicks).toEqual(['roles', 'cancel', 'confirm']);
  });
});
