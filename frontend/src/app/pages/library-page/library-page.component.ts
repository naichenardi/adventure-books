import {Component, DestroyRef, effect, inject, signal} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {debounceTime, Subject, switchMap} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {BookCardComponent} from '../../components/book-card/book-card.component';
import {FilterBarComponent} from '../../components/filter-bar/filter-bar.component';
import {HeroBannerComponent} from '../../components/hero-banner/hero-banner.component';
import {Book, Difficulty} from '../../models/adventure';
import {AdventureApiService} from '../../services/adventure-api.service';
import {SavedSessionService} from '../../services/saved-session.service';

type DifficultyFilter = 'ALL' | Difficulty;

@Component({
  selector: 'app-library-page',
  imports: [HeroBannerComponent, FilterBarComponent, BookCardComponent, RouterLink],
  templateUrl: './library-page.component.html',
  styleUrl: './library-page.component.scss'
})
export class LibraryPageComponent {
  private readonly api = inject(AdventureApiService);
  private readonly router = inject(Router);
  private readonly savedSessionService = inject(SavedSessionService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload$ = new Subject<void>();

  protected readonly books = signal<Book[]>([]);
  protected readonly loading = signal(true);
  protected readonly openingBookId = signal<number | null>(null);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly filters = ['All', 'Easy', 'Medium', 'Hard'];
  protected readonly selectedFilter = signal<DifficultyFilter>('ALL');
  protected readonly query = signal('');
  protected readonly availableCount = signal(0);

  constructor() {
    this.reload$
      .pipe(
        debounceTime(250),
        switchMap(() => {
          this.loading.set(true);
          this.errorMessage.set(null);

          const difficulty: Difficulty | undefined =
            this.selectedFilter() === 'ALL' ? undefined : (this.selectedFilter() as Difficulty);
          return this.api.listBooks(this.query(), difficulty);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (books) => {
          this.books.set(books);
          this.availableCount.set(books.length);
          this.loading.set(false);
        },
        error: (error) => {
          const message = error?.error?.message || 'Could not load adventures from API.';
          this.errorMessage.set(message);
          this.books.set([]);
          this.availableCount.set(0);
          this.loading.set(false);
        }
      });

    effect(() => {
      this.query();
      this.selectedFilter();
      this.reload$.next();
    });
  }

  protected selectFilter(filter: string): void {
    const normalized = filter.toUpperCase();
    if (normalized === 'ALL' || normalized === 'EASY' || normalized === 'MEDIUM' || normalized === 'HARD') {
      this.selectedFilter.set(normalized as DifficultyFilter);
    }
  }

  protected filterLabel(filter: DifficultyFilter): string {
    return filter.charAt(0) + filter.slice(1).toLowerCase();
  }

  protected openBook(book: Book): void {
    const bookId = book.id;

    if (!bookId || this.openingBookId() !== null) {
      return;
    }

    this.openingBookId.set(bookId);
    this.errorMessage.set(null);

    const savedSessionId = this.savedSessionService.getSavedSessionId(bookId);

    if (!savedSessionId) {
      this.navigateToNewSession(bookId);
      return;
    }

    this.api.getSessionById(savedSessionId).subscribe({
      next: (session) => {
        const canResume = session.saved && !session.finished && session.bookId === bookId;

        if (canResume) {
          this.router.navigate(['/play'], {queryParams: {sessionId: savedSessionId}}).finally(() => {
            this.openingBookId.set(null);
          });
          return;
        }

        this.savedSessionService.clearSavedSession(bookId);
        this.navigateToNewSession(bookId);
      },
      error: () => {
        this.savedSessionService.clearSavedSession(bookId);
        this.navigateToNewSession(bookId);
      }
    });
  }

  private navigateToNewSession(bookId: number): void {
    this.router.navigate(['/play', bookId]).finally(() => {
      this.openingBookId.set(null);
    });
  }
}
