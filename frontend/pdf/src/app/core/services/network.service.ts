import { Injectable, NgZone } from '@angular/core';
import { BehaviorSubject, fromEvent, merge, of } from 'rxjs';
import { mapTo, startWith } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class NetworkService {
  private _online$ = new BehaviorSubject<boolean>(navigator.onLine);

  online$ = merge(
    fromEvent(window, 'online').pipe(mapTo(true)),
    fromEvent(window, 'offline').pipe(mapTo(false)),
    of(navigator.onLine)
  ).pipe(startWith(navigator.onLine));

  constructor(private zone: NgZone) {
    window.addEventListener('online', () => this.zone.run(() => this._online$.next(true)));
    window.addEventListener('offline', () => this.zone.run(() => this._online$.next(false)));
  }

  get isOnline() { return this._online$.value; }
}
