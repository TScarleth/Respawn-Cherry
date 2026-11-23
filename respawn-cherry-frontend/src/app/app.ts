import { Location } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ForumFacade } from './core/forum.facade';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  readonly facade = inject(ForumFacade);
  readonly farewell = signal(false);
  readonly farewellHiding = signal(false);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private farewellTimer = 0;
  private hideTimer = 0;

  logout(): void {
    if (this.farewell()) return;
    this.farewell.set(true);
    window.clearTimeout(this.farewellTimer);
    this.farewellTimer = window.setTimeout(() => this.finishLogout(), 1600);
  }

  private finishLogout(): void {
    this.farewellHiding.set(true);
    this.facade.logout();
    void this.router.navigateByUrl('/login');
    window.clearTimeout(this.hideTimer);
    this.hideTimer = window.setTimeout(() => {
      this.farewell.set(false);
      this.farewellHiding.set(false);
    }, 460);
  }

  goBack(): void {
    if (window.history.length > 1) {
      this.location.back();
      return;
    }
    void this.router.navigateByUrl('/forum');
  }
}
