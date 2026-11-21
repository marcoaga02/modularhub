import { TestBed } from '@angular/core/testing';
import { Location, PlatformLocation } from '@angular/common';
import { MOCK_PLATFORM_LOCATION_CONFIG, MockPlatformLocation } from '@angular/common/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { OAuthService } from 'angular-oauth2-oidc';
import { App } from './app';

describe('App', () => {
  async function render(url = 'http://localhost/') {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideTranslateService(),
        MessageService,
        ConfirmationService,
        { provide: OAuthService, useValue: {} },
        { provide: PlatformLocation, useClass: MockPlatformLocation },
        { provide: MOCK_PLATFORM_LOCATION_CONFIG, useValue: { startUrl: url } },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('should render layout', async () => {
    const element = await render();

    expect(element.querySelector('app-topbar')).not.toBeNull();
    expect(element.querySelector('app-sidebar')).not.toBeNull();
    expect(element.querySelector('router-outlet')).not.toBeNull();
    expect(element.querySelector('p-toast')!.textContent).not.toContain(
      'core.auth.password-updated',
    );
  });

  it('should confirm the password update when coming back from the identity provider', async () => {
    const element = await render(
      'http://localhost/users?kc_action=UPDATE_PASSWORD&kc_action_status=success',
    );

    expect(element.querySelector('p-toast')!.textContent).toContain('core.auth.password-updated');
    expect(TestBed.inject(Location).path()).toBe('/users');
  });

  it('should not confirm anything when the password update was cancelled', async () => {
    const element = await render(
      'http://localhost/users?kc_action=UPDATE_PASSWORD&kc_action_status=cancelled',
    );

    expect(element.querySelector('p-toast')!.textContent).not.toContain(
      'core.auth.password-updated',
    );
  });
});
