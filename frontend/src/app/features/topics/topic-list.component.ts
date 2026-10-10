import { Component, DestroyRef, computed, inject, OnInit, signal } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { ActivatedRoute, RouterLink } from "@angular/router";
import { finalize } from "rxjs";
import { Topic } from "../../core/models/topic.models";
import { AuthService } from "../../core/services/auth.service";
import { TopicService } from "../../core/services/topic.service";
import { TopicDeleteDialogComponent } from "./topic-delete-dialog.component";
import { canManageTopics, hasAvailableSlots, normalizedSearch, topicErrorMessage,
  topicOccupancy, topicStatusLabel } from "./topic.utils";

@Component({
  selector: "app-topic-list",
  standalone: true,
  imports: [RouterLink, TopicDeleteDialogComponent],
  templateUrl: "./topic-list.component.html",
  styleUrls: ["./topic-page.css", "./topic-list.component.css"],
})
export class TopicListComponent implements OnInit {
  private readonly api = inject(TopicService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  readonly topics = signal<Topic[]>([]);
  readonly loading = signal(false);
  readonly error = signal("");
  readonly notice = signal(this.route.snapshot.queryParamMap.get("notice") === "deleted"
    ? "Đã xóa đề tài thành công." : "");
  readonly query = signal("");
  readonly field = signal("");
  readonly availability = signal("");
  readonly page = signal(1);
  readonly pageSize = 9;
  readonly deleteTarget = signal<Topic | null>(null);
  readonly canManage = computed(() => canManageTopics(this.auth.session()?.role));
  readonly availableCount = computed(() => this.topics().filter(hasAvailableSlots).length);
  readonly approvedCount = computed(() => this.topics().reduce((sum, t) => sum + t.currentStudents, 0));
  readonly fields = computed(() => [...new Set(this.topics().map(t => t.field).filter(
    (field): field is string => !!field?.trim(),
  ))].sort((a, b) => a.localeCompare(b, "vi")));
  readonly filteredTopics = computed(() => {
    const query = normalizedSearch(this.query());
    return this.topics().filter(topic => {
      const matchesText = !query || normalizedSearch(
        `${topic.topicCode} ${topic.topicName} ${topic.field ?? ""}`,
      ).includes(query);
      const filter = this.availability();
      const matchesStatus = !filter || (filter === "available" && hasAvailableSlots(topic))
        || (filter === "full" && topic.status === "OPEN" && !hasAvailableSlots(topic))
        || (filter === "closed" && topic.status !== "OPEN");
      return matchesText && matchesStatus && (!this.field() || topic.field === this.field());
    }).sort((a, b) => b.id - a.id);
  });
  readonly pageCount = computed(() => Math.max(1, Math.ceil(this.filteredTopics().length / this.pageSize)));
  readonly currentPage = computed(() => Math.min(this.page(), this.pageCount()));
  readonly visibleTopics = computed(() => this.filteredTopics().slice(
    (this.currentPage() - 1) * this.pageSize, this.currentPage() * this.pageSize,
  ));
  readonly hasFilters = computed(() => !!this.query() || !!this.field() || !!this.availability());
  readonly statusLabel = topicStatusLabel;
  readonly hasSlots = hasAvailableSlots;
  readonly occupancy = topicOccupancy;

  ngOnInit(): void { this.load(); }

  load(): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.error.set("");
    this.api.getAll().pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.loading.set(false)),
    ).subscribe({
      next: topics => this.topics.set(topics),
      error: (error: unknown) => this.error.set(topicErrorMessage(error, "load")),
    });
  }

  setFilter(key: "query" | "field" | "availability", value: string): void {
    this[key].set(value);
    this.page.set(1);
  }

  resetFilters(): void {
    this.query.set(""); this.field.set(""); this.availability.set(""); this.page.set(1);
  }

  deleted(id: number): void {
    this.deleteTarget.set(null);
    this.topics.update(topics => topics.filter(topic => topic.id !== id));
    this.notice.set("Đã xóa đề tài thành công.");
  }
}
