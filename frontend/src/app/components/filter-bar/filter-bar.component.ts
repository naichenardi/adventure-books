import {Component, input, output} from '@angular/core';

@Component({
  selector: 'app-filter-bar',
  templateUrl: './filter-bar.component.html',
  styleUrl: './filter-bar.component.scss'
})
export class FilterBarComponent {
  readonly filters = input.required<string[]>();
  readonly selected = input.required<string>();
  readonly selectedChange = output<string>();
}
