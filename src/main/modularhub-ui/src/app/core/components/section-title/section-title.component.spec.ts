import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SectionTitleComponent } from 'app/core/components/section-title/section-title.component';

@Component({
  imports: [SectionTitleComponent],
  template: '<app-section-title icon="pi pi-user">Account</app-section-title>',
})
class HostComponent {}

describe('SectionTitleComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let element: HTMLElement;

  beforeEach(async () => {
    fixture = TestBed.createComponent(HostComponent);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  it('should show the icon and the projected title', () => {
    const title = element.querySelector('app-section-title')!;

    expect(title.querySelector('i')!.className).toBe('pi pi-user');
    expect(title.textContent?.trim()).toBe('Account');
  });
});
