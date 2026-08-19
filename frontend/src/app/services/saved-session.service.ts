import {Injectable} from '@angular/core';

@Injectable({providedIn: 'root'})
export class SavedSessionService {
  private readonly storageKey = 'adventure-books.saved-sessions';

  getSavedSessionId(bookId: number): number | null {
    const map = this.readMap();
    const sessionId = map[String(bookId)];
    return typeof sessionId === 'number' ? sessionId : null;
  }

  setSavedSessionId(bookId: number, sessionId: number): void {
    const map = this.readMap();
    map[String(bookId)] = sessionId;
    this.writeMap(map);
  }

  clearSavedSession(bookId: number): void {
    const map = this.readMap();
    delete map[String(bookId)];
    this.writeMap(map);
  }

  private readMap(): Record<string, number> {
    if (typeof localStorage === 'undefined') {
      return {};
    }

    try {
      const rawValue = localStorage.getItem(this.storageKey);
      if (!rawValue) {
        return {};
      }

      const parsed = JSON.parse(rawValue) as Record<string, number>;
      return parsed && typeof parsed === 'object' ? parsed : {};
    } catch {
      return {};
    }
  }

  private writeMap(map: Record<string, number>): void {
    if (typeof localStorage === 'undefined') {
      return;
    }

    localStorage.setItem(this.storageKey, JSON.stringify(map));
  }
}
