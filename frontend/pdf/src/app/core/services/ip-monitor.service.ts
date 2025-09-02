import { Injectable } from '@angular/core';


@Injectable({ providedIn: 'root' })
export class IpMonitorService {
  async getIp(): Promise<string | null> {
    return null;
  }
}
