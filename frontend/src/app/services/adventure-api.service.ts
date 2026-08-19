import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Book, Difficulty, GameSession} from '../models/adventure';

@Injectable({providedIn: 'root'})
export class AdventureApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api';

  listBooks(query?: string, difficulty?: Difficulty) {
    let params = new HttpParams();

    if (query && query.trim().length > 0) {
      params = params.set('query', query.trim());
    }

    if (difficulty) {
      params = params.set('difficulty', difficulty);
    }

    return this.http.get<Book[]>(`${this.baseUrl}/books`, {params});
  }

  getBookById(id: number) {
    return this.http.get<Book>(`${this.baseUrl}/books/${id}`);
  }

  uploadBook(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Book>(`${this.baseUrl}/books/upload`, formData);
  }

  startSession(bookId: number) {
    return this.http.post<GameSession>(`${this.baseUrl}/sessions`, {bookId});
  }

  getSessionById(id: number) {
    return this.http.get<GameSession>(`${this.baseUrl}/sessions/${id}`);
  }

  chooseOption(sessionId: number, optionIndex: number) {
    return this.http.post<GameSession>(`${this.baseUrl}/sessions/${sessionId}/choose`, {optionIndex});
  }

  saveSession(sessionId: number) {
    return this.http.post<GameSession>(`${this.baseUrl}/sessions/${sessionId}/save`, {});
  }
}
