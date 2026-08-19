import {Component, computed, DestroyRef, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {combineLatest, forkJoin, map, switchMap, throwError} from 'rxjs';
import {ChoiceCardComponent} from '../../components/choice-card/choice-card.component';
import {Book, GameSession, Section} from '../../models/adventure';
import {AdventureApiService} from '../../services/adventure-api.service';
import {SavedSessionService} from '../../services/saved-session.service';

@Component({
  selector: 'app-game-page',
  imports: [RouterLink, ChoiceCardComponent],
  templateUrl: './game-page.component.html',
  styleUrl: './game-page.component.scss'
})
export class GamePageComponent {
  private readonly api = inject(AdventureApiService);
  private readonly savedSessionService = inject(SavedSessionService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly book = signal<Book | null>(null);
  protected readonly session = signal<GameSession | null>(null);
  protected readonly loading = signal(true);
  protected readonly actionInProgress = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly resumeUrl = signal<string | null>(null);
  protected readonly saveStatusMessage = signal<string | null>(null);

  protected readonly currentSection = computed<Section | null>(() => {
    const activeBook = this.book();
    const activeSession = this.session();

    if (!activeBook?.sections || !activeSession?.currentSectionId) {
      return null;
    }

    return activeBook.sections.find((section) => section.id === activeSession.currentSectionId) ?? null;
  });

  protected readonly sectionTitle = computed(() => {
    const section = this.currentSection();

    if (!section) {
      return 'Loading story section';
    }

    if (section.type === 'END') {
      return 'The End';
    }

    return `Section ${section.id}`;
  });

  protected readonly sectionParagraphs = computed(() => {
    const text = this.currentSection()?.text ?? '';
    return text
      .split(/\n\s*\n/g)
      .map((part) => part.trim())
      .filter((part) => part.length > 0);
  });

  protected readonly canChoose = computed(() => {
    const activeSession = this.session();
    return Boolean(activeSession?.id) && !activeSession?.finished && !this.actionInProgress();
  });

  constructor() {
    combineLatest([this.route.paramMap, this.route.queryParamMap])
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(([params, queryParams]) => {
        const rawSessionId = queryParams.get('sessionId');
        const sessionId = Number(rawSessionId);

        if (rawSessionId) {
          if (Number.isNaN(sessionId)) {
            this.errorMessage.set('Invalid session id in query params.');
            this.loading.set(false);
            return;
          }

          this.loadExistingSession(sessionId);
          return;
        }

        const rawBookId = params.get('bookId');
        const bookId = Number(rawBookId);

        if (!rawBookId || Number.isNaN(bookId)) {
          this.errorMessage.set('Invalid book id in route.');
          this.loading.set(false);
          return;
        }

        this.startNewGame(bookId);
      });
  }

  protected chooseOption(optionIndex: number): void {
    const sessionId = this.session()?.id;
    if (!sessionId || !this.canChoose()) {
      return;
    }

    this.actionInProgress.set(true);
    this.errorMessage.set(null);

    this.api.chooseOption(sessionId, optionIndex).subscribe({
      next: (session) => {
        this.session.set(session);
        this.actionInProgress.set(false);
      },
      error: (error) => {
        this.errorMessage.set(error?.error?.message || 'Unable to apply choice.');
        this.actionInProgress.set(false);
      }
    });
  }

  protected saveProgress(): void {
    const sessionId = this.session()?.id;
    if (!sessionId || this.actionInProgress()) {
      return;
    }

    this.actionInProgress.set(true);
    this.errorMessage.set(null);
    this.saveStatusMessage.set(null);

    this.api.saveSession(sessionId).subscribe({
      next: async (session) => {
        this.session.set(session);
        this.syncSavedSession(session);
        await this.updateResumeUrlAndCopy(session.id);
        this.actionInProgress.set(false);
      },
      error: (error) => {
        this.errorMessage.set(error?.error?.message || 'Unable to save progress.');
        this.actionInProgress.set(false);
      }
    });
  }

  private startNewGame(bookId: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      book: this.api.getBookById(bookId),
      session: this.api.startSession(bookId)
    }).subscribe({
      next: ({book, session}) => {
        this.book.set(book);
        this.session.set(session);
        this.syncSavedSession(session);
        this.loading.set(false);
      },
      error: (error) => {
        this.errorMessage.set(error?.error?.message || 'Unable to load game session.');
        this.loading.set(false);
      }
    });
  }

  private loadExistingSession(sessionId: number): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.api.getSessionById(sessionId)
      .pipe(
        switchMap((session) => {
          if (!session.bookId) {
            return throwError(() => new Error('Session has no associated book id.'));
          }

          return this.api.getBookById(session.bookId).pipe(
            map((book) => ({book, session}))
          );
        })
      )
      .subscribe({
        next: ({book, session}) => {
          this.book.set(book);
          this.session.set(session);
          this.syncSavedSession(session);
          this.setResumeUrl(session.id);
          this.loading.set(false);
        },
        error: (error) => {
          this.errorMessage.set(error?.error?.message || error?.message || 'Unable to resume saved session.');
          this.loading.set(false);
        }
      });
  }

  private setResumeUrl(sessionId: number | undefined): void {
    if (!sessionId || typeof window === 'undefined') {
      this.resumeUrl.set(null);
      return;
    }

    const url = new URL('/play', window.location.origin);
    url.searchParams.set('sessionId', String(sessionId));
    this.resumeUrl.set(url.toString());
  }

  private async updateResumeUrlAndCopy(sessionId: number | undefined): Promise<void> {
    this.setResumeUrl(sessionId);
    const url = this.resumeUrl();

    if (!url) {
      this.saveStatusMessage.set('Progress saved. Resume link unavailable.');
      return;
    }

    const copied = await this.tryCopyToClipboard(url);

    if (copied) {
      this.saveStatusMessage.set('Progress saved. Resume link copied to clipboard.');
      return;
    }

    this.saveStatusMessage.set('Progress saved. Copy the resume link below.');
  }

  private async tryCopyToClipboard(value: string): Promise<boolean> {
    try {
      if (typeof navigator === 'undefined' || !navigator.clipboard) {
        return false;
      }

      await navigator.clipboard.writeText(value);
      return true;
    } catch {
      return false;
    }
  }

  private syncSavedSession(session: GameSession): void {
    const resolvedBookId = session.bookId ?? this.book()?.id;
    const resolvedSessionId = session.id;

    if (!resolvedBookId || !resolvedSessionId) {
      return;
    }

    if (session.saved) {
      this.savedSessionService.setSavedSessionId(resolvedBookId, resolvedSessionId);
      return;
    }

    this.savedSessionService.clearSavedSession(resolvedBookId);
  }
}
