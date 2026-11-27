import { Component } from '@angular/core';

import { routeTitle } from '../../core/routing/route-title';

@Component({
  selector: 'app-account',
  templateUrl: './account.html',
})
export class Account {
  protected readonly title = routeTitle();
}
