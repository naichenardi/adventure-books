import {Component, input, output} from '@angular/core';
import {StoryOption} from '../../models/adventure';

@Component({
  selector: 'app-choice-card',
  templateUrl: './choice-card.component.html',
  styleUrl: './choice-card.component.scss'
})
export class ChoiceCardComponent {
  readonly option = input.required<StoryOption>();
  readonly index = input.required<number>();
  readonly disabled = input(false);
  readonly choose = output<number>();
}
