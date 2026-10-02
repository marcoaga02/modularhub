import {TestBed} from '@angular/core/testing';
import {App} from './app';
import {TranslateService} from '@ngx-translate/core';
import {MessageService} from 'primeng/api';
import {OAuthService} from 'angular-oauth2-oidc';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: TranslateService, useValue: {} },
        { provide: MessageService, useValue: {} },
        { provide: OAuthService, useValue: {} },
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render layout', () => {
    const fixture = TestBed.createComponent(App);
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('app-topbar')).not.toBeNull();
    expect(compiled.querySelector('app-sidebar')).not.toBeNull();
    expect(compiled.querySelector('router-outlet')).not.toBeNull();
  });
});
