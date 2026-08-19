import {Component, inject, signal} from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import {Book} from '../../models/adventure';
import {AdventureApiService} from '../../services/adventure-api.service';

@Component({
  selector: 'app-add-book-page',
  imports: [RouterLink],
  templateUrl: './add-book-page.component.html',
  styleUrl: './add-book-page.component.scss'
})
export class AddBookPageComponent {
  private readonly api = inject(AdventureApiService);
  private readonly router = inject(Router);

  protected readonly selectedFile = signal<File | null>(null);
  protected readonly isDragging = signal(false);
  protected readonly uploading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly createdBook = signal<Book | null>(null);

  protected onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragging.set(true);
  }

  protected onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragging.set(false);
  }

  protected onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragging.set(false);
    const file = event.dataTransfer?.files?.[0];
    if (file) {
      this.selectFile(file);
    }
  }

  protected onFileInputChange(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      this.selectFile(file);
    }
    (event.target as HTMLInputElement).value = '';
  }

  protected removeFile(): void {
    this.selectedFile.set(null);
  }

  protected upload(): void {
    const file = this.selectedFile();
    if (!file || this.uploading()) {
      return;
    }

    this.uploading.set(true);
    this.errorMessage.set(null);
    this.createdBook.set(null);

    this.api.uploadBook(file).subscribe({
      next: (book) => {
        this.uploading.set(false);
        this.createdBook.set(book);
        this.selectedFile.set(null);
      },
      error: (error) => {
        this.uploading.set(false);
        this.errorMessage.set(error?.error?.message || 'Could not upload this book. Please check the file and try again.');
      }
    });
  }

  protected addAnother(): void {
    this.createdBook.set(null);
    this.errorMessage.set(null);
  }

  protected playNow(): void {
    const bookId = this.createdBook()?.id;
    if (bookId) {
      this.router.navigate(['/play', bookId]);
    }
  }

  private selectFile(file: File): void {
    this.errorMessage.set(null);
    if (!file.name.toLowerCase().endsWith('.json')) {
      this.errorMessage.set('Please choose a .json file.');
      return;
    }
    this.selectedFile.set(file);
  }
}
