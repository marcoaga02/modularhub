import {ComponentFixture, TestBed} from '@angular/core/testing';

import {GroupSelectorComponent} from './group-selector.component';
import {DynamicDialogConfig, DynamicDialogRef} from 'primeng/dynamicdialog';
import {Subject} from 'rxjs';
import {TranslateService} from '@ngx-translate/core';

describe('GroupSelectorComponent', () => {
  let component: GroupSelectorComponent;
  let fixture: ComponentFixture<GroupSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupSelectorComponent],
      providers: [
        { provide: DynamicDialogRef, useValue: { close: () => {} } },
        { provide: TranslateService, useValue: {} },
        { provide: DynamicDialogConfig, useValue: {
            data: {
              formSubmit: new Subject(),
              groups: [],
              selectedGroupIds: []
            }
          }},
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(GroupSelectorComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
