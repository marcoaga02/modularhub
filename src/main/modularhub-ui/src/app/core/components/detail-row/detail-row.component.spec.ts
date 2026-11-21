import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DetailRowComponent } from 'app/core/components/detail-row/detail-row.component';

@Component({
  imports: [DetailRowComponent],
  template: '<dl><div app-detail-row label="Email">mario@test.it</div></dl>',
})
class HostComponent {}

describe('DetailRowComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let element: HTMLElement;

  beforeEach(async () => {
    fixture = TestBed.createComponent(HostComponent);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  it('should show the label as term and the projected content as description', () => {
    expect(element.querySelector('dt')!.textContent?.trim()).toBe('Email');
    expect(element.querySelector('dd')!.textContent?.trim()).toBe('mario@test.it');
  });
});
