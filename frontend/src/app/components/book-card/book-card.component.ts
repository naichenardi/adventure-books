import {Component, input, output} from '@angular/core';
import {Book} from '../../models/adventure';

@Component({
  selector: 'app-book-card',
  templateUrl: './book-card.component.html',
  styleUrl: './book-card.component.scss'
})
export class BookCardComponent {
  readonly book = input.required<Book>();
  readonly opening = input(false);
  readonly openBook = output<void>();

  protected readonly difficultyLabel: Record<string, string> = {
    EASY: 'Easy',
    MEDIUM: 'Medium',
    HARD: 'Hard'
  };

  protected summaryText(book: Book): string {
    const beginSection = book.sections?.find((section) => section.type === 'BEGIN') ?? book.sections?.[0];
    return beginSection?.text ?? 'No section text available yet for this adventure.';
  }

  protected endingCount(book: Book): number {
    return book.sections?.filter((section) => section.type === 'END').length ?? 0;
  }

  protected handleOpen(): void {
    if (this.opening() || !this.book().id) {
      return;
    }

    this.openBook.emit();
  }
}
