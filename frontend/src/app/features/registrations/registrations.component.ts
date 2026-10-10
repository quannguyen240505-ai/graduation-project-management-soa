import { CommonModule } from "@angular/common";
import { HttpErrorResponse } from "@angular/common/http";
import { Component, computed, inject, OnInit, signal } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { AuthService } from "../../core/services/auth.service";
import { RegistrationService } from "../../core/services/registration.service";
import {
  Registration,
  RegistrationStatus,
  Topic,
} from "../../core/models/registration.models";

type PageTab = "topics" | "registrations";
type PendingAction =
  | { kind: "create"; topic: Topic }
  | { kind: "approve" | "reject" | "delete"; registration: Registration };

@Component({
  selector: "app-registrations",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./registrations.component.html",
  styleUrl: "./registrations.component.css",
})
export class RegistrationsComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly api = inject(RegistrationService);

  readonly session = this.auth.session;
  readonly isStudent = computed(() => this.session()?.role === "STUDENT");
  readonly isManager = computed(() =>
    ["LECTURER", "ADMIN"].includes(this.session()?.role ?? ""),
  );
  readonly activeTab = signal<PageTab>("topics");
  readonly topics = signal<Topic[]>([]);
  readonly registrations = signal<Registration[]>([]);
  readonly loadingTopics = signal(false);
  readonly loadingRegistrations = signal(false);
  readonly actionLoading = signal(false);
  readonly topicsError = signal("");
  readonly registrationsError = signal("");
  readonly actionError = signal("");
  readonly notice = signal("");
  readonly search = signal("");
  readonly fieldFilter = signal("ALL");
  readonly topicFilter = signal("ALL");
  readonly registrationFilter = signal("ALL");
  readonly registrationSearch = signal("");
  readonly selectedTopic = signal<Topic | null>(null);
  readonly pendingAction = signal<PendingAction | null>(null);
  note = "";

  readonly fields = computed(() =>
    [...new Set(this.topics().map((topic) => topic.field?.trim()).filter(
      (field): field is string => !!field,
    ))].sort((a, b) => a.localeCompare(b, "vi")),
  );

  readonly myRegistrations = computed(() => {
    const id = this.session()?.userId;
    return this.registrations().filter((registration) => registration.studentId === id);
  });

  readonly pendingCount = computed(() =>
    this.registrations().filter((r) => r.status === "PENDING").length,
  );
  readonly approvedCount = computed(() =>
    this.registrations().filter((r) => r.status === "APPROVED").length,
  );
  readonly myApprovedCount = computed(() =>
    this.myRegistrations().filter((r) => r.status === "APPROVED").length,
  );
  readonly openTopicCount = computed(() =>
    this.topics().filter((topic) => this.isAvailable(topic)).length,
  );

  readonly visibleTopics = computed(() => {
    const keyword = this.normalize(this.search());
    return this.topics().filter((topic) => {
      const matchesKeyword = !keyword || this.normalize(
        [topic.topicName, topic.topicCode, topic.field, topic.description].join(" "),
      ).includes(keyword);
      const matchesField = this.fieldFilter() === "ALL" || topic.field === this.fieldFilter();
      const filter = this.topicFilter();
      const matchesState = filter === "ALL" ||
        (filter === "AVAILABLE" && this.isAvailable(topic)) ||
        (filter === "UNAVAILABLE" && !this.isAvailable(topic));
      return matchesKeyword && matchesField && matchesState;
    });
  });

  readonly visibleRegistrations = computed(() => {
    const keyword = this.normalize(this.registrationSearch());
    const source = this.isStudent() ? this.myRegistrations() : this.registrations();
    return source
      .filter((registration) => {
        const matchesStatus = this.registrationFilter() === "ALL" ||
          registration.status === this.registrationFilter();
        const topic = this.topicById(registration.topicId);
        const matchesKeyword = !keyword || this.normalize(
          [registration.id, registration.studentId, registration.topicId,
            topic?.topicCode, topic?.topicName, registration.note].join(" "),
        ).includes(keyword);
        return matchesStatus && matchesKeyword;
      })
      .sort((a, b) => b.id - a.id);
  });

  ngOnInit(): void {
    if (!this.isStudent() && !this.isManager()) return;
    this.activeTab.set(this.isManager() ? "registrations" : "topics");
    this.reload();
  }

  reload(): void {
    this.loadTopics();
    this.loadRegistrations();
  }

  loadTopics(): void {
    this.loadingTopics.set(true);
    this.topicsError.set("");
    this.api.getTopics().subscribe({
      next: (topics) => {
        this.topics.set(topics);
        this.loadingTopics.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.topicsError.set(this.describeError(error, "Không thể tải danh sách đề tài."));
        this.loadingTopics.set(false);
      },
    });
  }

  loadRegistrations(): void {
    this.loadingRegistrations.set(true);
    this.registrationsError.set("");
    this.api.getRegistrations().subscribe({
      next: (items) => {
        this.registrations.set(items);
        this.loadingRegistrations.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.registrationsError.set(this.describeError(error, "Không thể tải danh sách đăng ký."));
        this.loadingRegistrations.set(false);
      },
    });
  }

  selectTab(tab: PageTab): void {
    this.activeTab.set(tab);
    this.actionError.set("");
    this.notice.set("");
  }

  openCreate(topic: Topic): void {
    if (!this.isStudent() || !this.isAvailable(topic) || this.wasRegistered(topic.id)) return;
    this.note = "";
    this.actionError.set("");
    this.notice.set("");
    this.selectedTopic.set(topic);
  }

  closeCreate(): void {
    if (this.actionLoading()) return;
    this.selectedTopic.set(null);
    this.note = "";
  }

  requestCreate(): void {
    const topic = this.selectedTopic();
    if (!topic || !this.isStudent() || this.actionLoading()) return;
    const userId = this.session()?.userId;
    if (userId == null || !Number.isSafeInteger(userId) || userId <= 0) {
      this.actionError.set("Không xác định được tài khoản đăng nhập. Vui lòng đăng nhập lại.");
      return;
    }
    if (!this.isAvailable(topic) || this.wasRegistered(topic.id)) {
      this.actionError.set("Đề tài không còn đủ điều kiện đăng ký. Hãy làm mới danh sách.");
      return;
    }
    this.pendingAction.set({ kind: "create", topic });
  }

  requestManage(kind: "approve" | "reject" | "delete", registration: Registration): void {
    if (!this.isManager() || this.actionLoading()) return;
    // Backend currently permits changing status more than once; UI only shows decisions for PENDING.
    if (kind !== "delete" && registration.status !== "PENDING") return;
    this.actionError.set("");
    this.pendingAction.set({ kind, registration });
  }

  closeConfirm(): void {
    if (!this.actionLoading()) this.pendingAction.set(null);
  }

  confirmAction(): void {
    const action = this.pendingAction();
    if (!action || this.actionLoading()) return;
    this.actionError.set("");
    this.notice.set("");
    this.actionLoading.set(true);

    if (action.kind === "create") {
      const userId = this.session()?.userId;
      if (!this.isStudent() || userId == null) {
        this.actionLoading.set(false);
        this.pendingAction.set(null);
        this.actionError.set("Phiên đăng nhập không hợp lệ.");
        return;
      }
      this.api.createRegistration({
        // No editable studentId field. Backend must check JWT; FE validation alone is insufficient.
        studentId: userId,
        topicId: action.topic.id,
        note: this.note.trim(),
      }).subscribe({
        next: () => {
          this.finishAction("Đã gửi đăng ký. Hồ sơ đang chờ xét duyệt.");
          this.selectedTopic.set(null);
          this.note = "";
          this.activeTab.set("registrations");
          this.reload();
        },
        error: (error: HttpErrorResponse) => this.failAction(error),
      });
      return;
    }

    const { registration, kind } = action;
    if (!this.isManager()) {
      this.actionLoading.set(false);
      this.pendingAction.set(null);
      return;
    }
    if (kind === "delete") {
      this.api.deleteRegistration(registration.id).subscribe({
        next: () => {
          this.finishAction(`Đã xóa đăng ký #${registration.id}.`);
          this.reload();
        },
        error: (error: HttpErrorResponse) => this.failAction(error),
      });
    } else {
      const status: RegistrationStatus = kind === "approve" ? "APPROVED" : "REJECTED";
      this.api.updateStatus(registration.id, status).subscribe({
        next: () => {
          this.finishAction(status === "APPROVED" ? "Đã duyệt đăng ký." : "Đã từ chối đăng ký.");
          this.reload();
        },
        error: (error: HttpErrorResponse) => this.failAction(error),
      });
    }
  }

  actionTitle(): string {
    const action = this.pendingAction();
    if (!action) return "Xác nhận thao tác";
    switch (action.kind) {
      case "create": return "Xác nhận đăng ký đề tài";
      case "approve": return "Duyệt đăng ký này?";
      case "reject": return "Từ chối đăng ký này?";
      case "delete": return "Xóa đăng ký này?";
    }
  }

  actionDescription(): string {
    const action = this.pendingAction();
    if (!action) return "";
    if (action.kind === "create") {
      return `Gửi yêu cầu đăng ký “${action.topic.topicName}” cho tài khoản hiện tại (ID ${this.session()?.userId}).`;
    }
    if (action.kind === "delete") return `Đăng ký #${action.registration.id} sẽ bị xóa khỏi hệ thống. Không thể hoàn tác.`;
    return `Đăng ký #${action.registration.id} của sinh viên #${action.registration.studentId} sẽ được ${action.kind === "approve" ? "duyệt" : "từ chối"}.`;
  }

  occupancyPercent(topic: Topic): number {
    return topic.maxStudents > 0
      ? Math.min(100, Math.max(0, (topic.currentStudents / topic.maxStudents) * 100))
      : 100;
  }

  isAvailable(topic: Topic): boolean {
    return topic.status === "OPEN" && topic.currentStudents < topic.maxStudents;
  }

  wasRegistered(topicId: number): boolean {
    return this.myRegistrations().some((item) => item.topicId === topicId);
  }

  topicById(id: number): Topic | undefined {
    return this.topics().find((topic) => topic.id === id);
  }

  topicTitle(id: number): string {
    return this.topicById(id)?.topicName ?? `Đề tài #${id}`;
  }

  statusLabel(status: string): string {
    switch (status) {
      case "PENDING": return "Chờ duyệt";
      case "APPROVED": return "Đã duyệt";
      case "REJECTED": return "Từ chối";
      default: return status;
    }
  }

  statusClass(status: string): string {
    switch (status) {
      case "PENDING": return "badge-pending";
      case "APPROVED": return "badge-approved";
      case "REJECTED": return "badge-rejected";
      default: return "badge-neutral";
    }
  }

  dismissMessage(): void {
    this.actionError.set("");
    this.notice.set("");
  }

  private finishAction(message: string): void {
    this.actionLoading.set(false);
    this.pendingAction.set(null);
    this.notice.set(message);
  }

  private failAction(error: HttpErrorResponse): void {
    this.actionLoading.set(false);
    this.pendingAction.set(null);
    this.actionError.set(this.describeError(error, "Thao tác không thành công. Vui lòng thử lại."));
  }

  private normalize(value: string): string {
    return value.toLocaleLowerCase("vi").trim();
  }

  private describeError(error: HttpErrorResponse, fallback: string): string {
    const body = error.error;
    const apiMessage = body && typeof body === "object" && typeof body.message === "string"
      ? body.message : "";
    switch (error.status) {
      case 0: return "Không kết nối được API Gateway. Kiểm tra backend tại cổng 8080.";
      case 400: return apiMessage || "Dữ liệu gửi lên không hợp lệ.";
      case 401: return "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.";
      case 403: return "Bạn không có quyền thực hiện thao tác này.";
      case 404: return "Đăng ký hoặc đề tài không còn tồn tại.";
      case 409: return apiMessage || "Đăng ký bị xung đột: có thể đã đăng ký, đề tài đã đóng hoặc hết chỗ.";
      case 502:
      case 503:
      case 504: return "Một dịch vụ backend đang không khả dụng. Vui lòng thử lại sau.";
      default: return apiMessage || fallback;
    }
  }
}
