import {ComponentFixture, TestBed} from '@angular/core/testing';

import {UserFormComponent} from './user-form.component';
import {BehaviorSubject, of, Subject} from 'rxjs';
import {DialogService, DynamicDialogConfig, DynamicDialogRef} from 'primeng/dynamicdialog';
import {ConfirmationService, MessageService} from 'primeng/api';
import {provideTranslateService} from '@ngx-translate/core';
import {RoleService} from 'app/modules/user-management/api/services/role.service';
import {UserService} from 'app/modules/user-management/api/services/user.service';
import {UUID} from 'app/core/api/models/uuid';

describe('UserFormComponent', () => {
  const roleId = 'role-id' as UUID;

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
        {provide: RoleService, useValue: {getRoles: () => of([])}},
        {provide: UserService, useValue: {}},
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  function closeRolesDialogWith(roleIds: UUID[] | undefined) {
    buttonClick.next('roles');
    dialogClose.next(roleIds);
  }

  function selectedRoleIdsPassedToNextDialog(): UUID[] {
    buttonClick.next('roles');
    const config = openedDialogs[openedDialogs.length - 1];
    return (config.data as {selectedRoleIds: UUID[]}).selectedRoleIds;
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should keep the selected roles when the roles dialog confirms them', () => {
    closeRolesDialogWith([roleId]);

    expect(selectedRoleIdsPassedToNextDialog()).toEqual([roleId]);
  });

  it('should clear the roles when the roles dialog confirms an empty selection', () => {
    closeRolesDialogWith([roleId]);

    closeRolesDialogWith([]);

    expect(selectedRoleIdsPassedToNextDialog()).toEqual([]);
  });

  it('should keep the previous roles when the roles dialog is closed without confirming', () => {
    closeRolesDialogWith([roleId]);

    closeRolesDialogWith(undefined);

    expect(selectedRoleIdsPassedToNextDialog()).toEqual([roleId]);
  });
});
