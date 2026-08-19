import {Component, input} from '@angular/core';

@Component({
  selector: 'app-hero-banner',
  templateUrl: './hero-banner.component.html',
  styleUrl: './hero-banner.component.scss'
})
export class HeroBannerComponent {
  readonly title = input.required<string>();
  readonly subtitle = input.required<string>();
  readonly stat = input.required<string>();
}
