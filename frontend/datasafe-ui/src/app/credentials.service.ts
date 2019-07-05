import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CredentialsService {

  private username: string;
  private password: string;

  constructor() { }

  public setCredentials(username: string, password: string) {
    this.username = username;
    this.password = password;
  }

  public getCredentialsForApi() {
    return {"userName": this.username, "password": this.password}
  }
}
