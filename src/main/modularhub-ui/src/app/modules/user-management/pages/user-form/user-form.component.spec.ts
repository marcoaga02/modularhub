import {ComponentFixture, TestBed} from '@angular/core/testing';

import {UserFormComponent} from './user-form.component';
import {BehaviorSubject, of, Subject} from 'rxjs';
import {DialogService, DynamicDialogConfig, DynamicDialogRef} from 'primeng/dynamicdialog';
import {ConfirmationService, MessageService} from 'primeng/api';
import {provideTranslateService} from '@ngx-translate/core';
import {GroupService} from 'app/modules/user-management/api/services/group.service';
import {UserService} from 'app/modules/user-management/api/services/user.service';
import {UUID} from 'app/core/api/models/uuid';

describe('UserFormComponent', () => {
  const groupId = 'group-id' as UUID;

  let component: UserFormComponent;
  let fixture: ComponentFixture<UserFormComponent>;
  let buttonClick: Subject<string>;
  let dialogClose: Subject<UUID[] | undefined>;
  let openedDialogs: DynamicDialogConfig[];

  beforeEach(async () => {
    buttonClick = new Subject<string>();
    openedDialogs = [];
    const dialogService = {
      open: (_component: unknown, config: DynamicDialogConfig) => {
        openedDialogs.push(config);
        dialogClose = new Subject<UUID[] | undefined>();
        return {onClose: dialogClose.asObservable()};
      }
    };

    await TestBed.configureTestingModule({
      imports: [UserFormComponent],
      providers: [
        provideTranslateService(),
        {
          provide: DynamicDialogConfig, useValue: {
            data: {
              formSubmit: new Subject(),
              formValid: new BehaviorSubject(false),
              buttonClick,
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
        {provide: DialogService, useValue: dialogService},
        {provide: MessageService, useValue: {}},
        {provide: GroupService, useValue: {getGroups: () => of([])}},
        {provide: UserService, useValue: {}},
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  function closeGroupsDialogWith(groupIds: UUID[] | undefined) {
    buttonClick.next('groups');
    dialogClose.next(groupIds);
  }

  function selectedGroupIdsPassedToNextDialog(): UUID[] {
    buttonClick.next('groups');
    const config = openedDialogs[openedDialogs.length - 1];
    return (config.data as {selectedGroupIds: UUID[]}).selectedGroupIds;
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should keep the selected groups when the groups dialog confirms them', () => {
    closeGroupsDialogWith([groupId]);

    expect(selectedGroupIdsPassedToNextDialog()).toEqual([groupId]);
  });

  it('should clear the groups when the groups dialog confirms an empty selection', () => {
    closeGroupsDialogWith([groupId]);

    closeGroupsDialogWith([]);

    expect(selectedGroupIdsPassedToNextDialog()).toEqual([]);
  });

  it('should keep the previous groups when the groups dialog is closed without confirming', () => {
    closeGroupsDialogWith([groupId]);

    closeGroupsDialogWith(undefined);

    expect(selectedGroupIdsPassedToNextDialog()).toEqual([groupId]);
  });
});
