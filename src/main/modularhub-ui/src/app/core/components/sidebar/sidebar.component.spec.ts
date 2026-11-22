import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { SidebarComponent } from 'app/core/components/sidebar/sidebar.component';
import { PluginService } from 'app/core/api/services/plugin.service';
import { AuthService } from 'app/core/auth/auth.service';
import { Plugin } from 'app/core/api/generated';

@Component({ template: '' })
class DummyComponent {}

describe('SidebarComponent', () => {
  const usersPlugin: Plugin = {
    path: 'users',
    name: 'users',
    description: 'users.description',
    icon: 'pi pi-users',
  };
  const reportsPlugin: Plugin = {
    path: 'reports',
    name: 'reports',
    description: 'reports.description',
    icon: 'pi pi-chart-bar',
  };

  let fixture: ComponentFixture<SidebarComponent>;
  let element: HTMLElement;
  let authService: { logout: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    authService = { logout: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [SidebarComponent],
      providers: [
        provideTranslateService(),
        provideRouter([
          { path: 'users', component: DummyComponent },
          { path: 'reports', component: DummyComponent },
        ]),
        { provide: PluginService, useValue: { plugins: signal([usersPlugin, reportsPlugin]) } },
        { provide: AuthService, useValue: authService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SidebarComponent);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  function pluginLinks(): HTMLAnchorElement[] {
    return Array.from(element.querySelectorAll<HTMLAnchorElement>('nav a'));
  }

  function nav(): HTMLElement {
    return element.querySelector('nav')!;
  }

  function logo(): HTMLImageElement | null {
    return element.querySelector<HTMLImageElement>('nav img[alt="ModularHub"]');
  }

  function toggleButton(): HTMLButtonElement {
    return element.querySelector<HTMLButtonElement>('nav button')!;
  }

  async function click(target: HTMLElement) {
    target.click();
    await fixture.whenStable();
  }

  async function hover(target: HTMLElement, type: 'mouseenter' | 'mouseleave') {
    target.dispatchEvent(new MouseEvent(type));
    await fixture.whenStable();
  }

  it('should start pinned with a link and a description for every plugin', () => {
    expect(pluginLinks().map((link) => link.getAttribute('href'))).toEqual(['/users', '/reports']);
    expect(logo()).not.toBeNull();
    expect(element.textContent).toContain('users.description');
    expect(element.textContent).toContain('reports.description');
    expect(element.classList).toContain('w-64');
    expect(toggleButton().getAttribute('aria-label')).toBe('core.components.sidebar.collapse');
  });

  it('should show only the icons when it is collapsed', async () => {
    await click(toggleButton());

    expect(pluginLinks()).toHaveLength(2);
    expect(element.textContent).not.toContain('users.description');
    expect(logo()).toBeNull();
    expect(element.classList).toContain('w-16');
    expect(toggleButton().getAttribute('aria-label')).toBe('core.components.sidebar.expand');
  });

  it('should float over the page without taking space when hovered while collapsed', async () => {
    await click(toggleButton());

    await hover(toggleButton(), 'mouseenter');

    expect(element.textContent).toContain('users.description');
    expect(logo()).not.toBeNull();
    expect(toggleButton().classList).toContain('order-first');
    expect(nav().classList).toContain('w-64');
    expect(nav().classList).toContain('shadow-xl');
    expect(element.classList).toContain('w-16');
  });

  it('should stay collapsed when the pointer enters the sidebar outside the toggle', async () => {
    await click(toggleButton());

    await hover(nav(), 'mouseenter');

    expect(element.textContent).not.toContain('users.description');
    expect(nav().classList).toContain('w-16');
  });

  it('should collapse again when the pointer leaves the floating sidebar', async () => {
    await click(toggleButton());
    await hover(toggleButton(), 'mouseenter');

    await hover(nav(), 'mouseleave');

    expect(element.textContent).not.toContain('users.description');
    expect(nav().classList).toContain('w-16');
  });

  it('should pin the floating sidebar when the toggle is clicked', async () => {
    await click(toggleButton());
    await hover(toggleButton(), 'mouseenter');

    await click(toggleButton());

    expect(element.classList).toContain('w-64');
    expect(nav().classList).not.toContain('shadow-xl');
    expect(element.textContent).toContain('users.description');
  });

  it('should navigate to the plugin and highlight its link', async () => {
    await click(pluginLinks()[1]);

    expect(TestBed.inject(Router).url).toBe('/reports');
    expect(pluginLinks()[1].classList).toContain('bg-highlight');
    expect(pluginLinks()[0].classList).not.toContain('bg-highlight');
  });

  it('should log out right away', async () => {
    await click(element.querySelector<HTMLElement>('nav i.pi-sign-out')!.closest('button')!);

    expect(authService.logout).toHaveBeenCalledOnce();
  });
});
