import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ToastService {
  success(msg: string) { this.toast(msg, 'success'); }
  error(msg: string) { this.toast(msg, 'error'); }
  warn(msg: string) { this.toast(msg, 'warn'); }

  private toast(msg: string, type: 'success'|'error'|'warn') {
    const el = document.createElement('div');
    el.textContent = msg;
    el.className = `toast ${type}`;
    Object.assign(el.style, {
      position: 'fixed', zIndex: '9999', right: '16px', top: '16px',
      background: type==='success' ? '#10b981' : type==='error' ? '#ef4444' : '#f59e0b',
      color: '#fff', padding: '10px 14px', borderRadius: '10px', boxShadow: 'var(--shadow)'
    } as CSSStyleDeclaration);
    document.body.appendChild(el);
    setTimeout(() => el.remove(), 2200);
  }
}
