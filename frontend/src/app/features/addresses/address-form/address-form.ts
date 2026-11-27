import { Component } from '@angular/core';

import { routeTitle } from '../../../core/routing/route-title';

@Component({
  selector: 'app-address-form',
  templateUrl: './address-form.html',
})
export class AddressForm {
  protected readonly title = routeTitle();
}
