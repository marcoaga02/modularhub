import { TestBed } from '@angular/core/testing';
import { Location, PlatformLocation } from '@angular/common';
import { MockPlatformLocation } from '@angular/common/testing';
import { takePasswordUpdateStatus } from 'app/core/auth/password-update';

describe('takePasswordUpdateStatus', () => {
  let location: Location;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{ provide: PlatformLocation, useClass: MockPlatformLocation }],
    });
    location = TestBed.inject(Location);
  });

  it('should return the status and remove the action parameters from the url', () => {
    location.go('/users', 'page=1&kc_action=UPDATE_PASSWORD&kc_action_status=success');

    expect(takePasswordUpdateStatus(location)).toBe('success');
    expect(location.path()).toBe('/users?page=1');
  });

  it('should return null and leave the url untouched when no action was performed', () => {
    location.go('/users', 'page=1');

    expect(takePasswordUpdateStatus(location)).toBeNull();
    expect(location.path()).toBe('/users?page=1');
  });
});
