import {
  AfterViewInit, Component, DestroyRef, ElementRef, EventEmitter,
  Input, Output, ViewChild, inject, signal,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { finalize } from "rxjs";
import { Topic } from "../../core/models/topic.models";
import { TopicService } from "../../core/services/topic.service";
import { topicErrorMessage } from "./topic.utils";

@Component({
  selector: "app-topic-delete-dialog",
  standalone: true,
  template: `
    <dialog #dialog aria-labelledby="delete-title" aria-describedby="delete-description"
      (cancel)="cancel($event)">
      <div class="dialog-content">
        <span class="danger-icon" aria-hidden="true">!</span>
        <h2 id="delete-title">Xóa đề tài?</h2>
        <p id="delete-description">Bạn sắp xóa <strong>{{ topic.topicCode }}</strong> —
          {{ topic.topicName }}. Thao tác này không thể hoàn tác.</p>
        @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
        <div class="dialog-actions">
          <button type="button" class="cancel" autofocus [disabled]="deleting()" (click)="cancel()">Giữ lại</button>
          <button type="button" class="delete" [disabled]="deleting()" (click)="confirm()">
            {{ deleting() ? "Đang xóa…" : "Xóa đề tài" }}
          </button>
        </div>
      </div>
    </dialog>
  `,
  styles: [`
    dialog { width: min(460px, calc(100vw - 32px)); border: 0; border-radius: 20px;
      padding: 0; color: #172033; box-shadow: 0 24px 80px #0f1b3340; }
    dialog::backdrop { background: #0f1b3380; }
    .dialog-content { padding: 28px; }
    .danger-icon { display: grid; place-items: center; width: 44px; height: 44px;
      border-radius: 14px; background: #fff0ee; color: #b42318; font-size: 24px; font-weight: 800; }
    h2 { margin: 18px 0 10px; font-size: 22px; }
    p { color: #626d82; line-height: 1.7; overflow-wrap: anywhere; }
    .dialog-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px; }
    button { padding: 11px 16px; border-radius: 10px; border: 1px solid #dce3ef;
      cursor: pointer; font-weight: 650; }
    .cancel { background: white; color: #172033; }
    .delete { background: #b42318; color: white; border-color: #b42318; }
    button:disabled { opacity: .6; cursor: wait; }
    .error { padding: 12px; color: #b42318; background: #fff0ee; border-radius: 10px; }
  `],
})
export class TopicDeleteDialogComponent implements AfterViewInit {
  @Input({ required: true }) topic!: Topic;
  @Output() readonly closed = new EventEmitter<void>();
  @Output() readonly deleted = new EventEmitter<number>();
  @ViewChild("dialog", { static: true }) private dialog!: ElementRef<HTMLDialogElement>;
  private readonly api = inject(TopicService);
  private readonly destroyRef = inject(DestroyRef);
  readonly deleting = signal(false);
  readonly error = signal("");

  ngAfterViewInit(): void { this.dialog.nativeElement.showModal(); }

  cancel(event?: Event): void {
    event?.preventDefault();
    if (!this.deleting()) this.closed.emit();
  }

  confirm(): void {
    if (this.deleting()) return;
    this.deleting.set(true);
    this.error.set("");
    this.api.delete(this.topic.id).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.deleting.set(false)),
    ).subscribe({
      next: () => this.deleted.emit(this.topic.id),
      error: (error: unknown) => this.error.set(topicErrorMessage(error, "delete")),
    });
  }
}
