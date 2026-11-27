import { Component, inject } from '@angular/core';
import { MatButton } from '@angular/material/button';
import { MatToolbar } from '@angular/material/toolbar';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../core/auth/auth.service';
import { AuthSession } from '../../core/auth/auth-session';

@Component({
  selector: 'app-shell',
  imports: [MatToolbar, MatButton, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './shell.html',
  styleUrl: './shell.css',
})
export class Shell {
  private readonly session = inject(AuthSession);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly isAuthenticated = this.session.isAuthenticated;
  protected readonly user = this.session.user;

  protected logout(): void {
    this.auth.logout();
    void this.router.navigate(['/login']);
  }
}
