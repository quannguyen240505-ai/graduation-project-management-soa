import { Component } from "@angular/core";
import { RouterLink } from "@angular/router";

@Component({
  selector: "app-forbidden",
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="box">
      <div class="code">403</div>
      <h2>Không có quyền truy cập</h2>
      <p>Gateway hoặc service đã từ chối request theo role hiện tại.</p>
      <a routerLink="/dashboard">Quay lại Dashboard</a>
    </div>
  `,
  styles: [
    `
      :host {
        min-height: 100vh;
        display: grid;
        place-items: center;
        background: #f5f7fb;
      }
      .box {
        text-align: center;
        background: white;
        border: 1px solid #e1e6ef;
        border-radius: 20px;
        padding: 46px;
        max-width: 460px;
      }
      .code {
        font-size: 64px;
        font-weight: 900;
        color: #315ee8;
      }
      h2 {
        margin: 8px 0;
      }
      p {
        color: #6b7280;
        line-height: 1.5;
      }
      a {
        display: inline-block;
        margin-top: 10px;
        text-decoration: none;
        background: #315ee8;
        color: white;
        padding: 11px 15px;
        border-radius: 10px;
        font-weight: 700;
      }
    `,
  ],
})
export class ForbiddenComponent {}
