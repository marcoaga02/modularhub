import {ComponentFixture, TestBed} from '@angular/core/testing';

import {RoleSelectorComponent} from './role-selector.component';
import {DynamicDialogConfig, DynamicDialogRef} from 'primeng/dynamicdialog';
import {Subject} from 'rxjs';
import {provideTranslateService} from '@ngx-translate/core';
import {RoleSummary} from 'app/modules/user-management/api/generated';
import {UUID} from 'app/core/api/models/uuid';

describe('RoleSelectorComponent', () => {
  const admin: RoleSummary = {id: 'admin-id' as UUID, name: 'ADMIN', system: true};
  const operators: RoleSummary = {id: 'operators-id' as UUID, name: 'Operators', system: false};

  let fixture: ComponentFixture<RoleSelectorComponent>;
  let formSubmit: Subject<void>;
  let closedWith: UUID[] | undefined;

  beforeEach(async () => {
    formSubmit = new Subject<void>();
    closedWith = undefined;

    await TestBed.configureTestingModule({
      imports: [RoleSelectorComponent],
      providers: [
        provideTranslateService(),
        {
          provide: DynamicDialogRef, useValue: {
            close: (result: UUID[]) => closedWith = result
          }
        },
        {
          provide: DynamicDialogConfig, useValue: {
            data: {
              formSubmit,
              roles: [admin, operators],
              selectedRoleIds: [operators.id]
            }
          }
        },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RoleSelectorComponent);
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should close with the ids of the roles already selected when the form is submitted', () => {
    formSubmit.next();

    expect(closedWith).toEqual([operators.id]);
  });

  it('should show the role names in the available and selected lists', () => {
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(text).toContain('ADMIN');
    expect(text).toContain('Operators');
  });
});
