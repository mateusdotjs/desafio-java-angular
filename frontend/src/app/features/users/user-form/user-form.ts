import { Component } from '@angular/core';

import { routeTitle } from '../../../core/routing/route-title';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.html',
})
export class UserForm {
  protected readonly title = routeTitle();
}
