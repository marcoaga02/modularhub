import {ComponentFixture, TestBed} from '@angular/core/testing';

import {HomePagePlaceholderComponent} from './home-page-placeholder.component';

describe('HomePagePlaceholderComponent', () => {
  let component: HomePagePlaceholderComponent;
  let fixture: ComponentFixture<HomePagePlaceholderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomePagePlaceholderComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(HomePagePlaceholderComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
