import { inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

export function routeTitle(): string {
  const value = inject(ActivatedRoute).snapshot.data['title'];
  return typeof value === 'string' ? value : '';
}
