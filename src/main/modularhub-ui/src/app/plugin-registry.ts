import {Route} from '@angular/router';

export const PLUGIN_REGISTRY: Record<string, Route['loadComponent']> = {
  // TODO rimuovere/aggiornare home ed aggiungere gli altri plugin
  'home': () =>
    import('app/modules/home-page-placeholder/home-page-placeholder.component')
      .then(m => m.HomePagePlaceholderComponent),
  'user-management': () =>
    import('app/modules/user-management/pages/user-list/user-list.component')
      .then(m => m.UserListComponent),
};
