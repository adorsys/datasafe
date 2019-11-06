import {Injectable} from '@angular/core';

export class Credentials {
  readonly username: string;
  readonly password: string;

  constructor(username: string, password: string) {
    this.username = username;
    this.password = password;
  }
}

@Injectable({
  providedIn: 'root'
})
export class CredentialsService {

  private credentials: Credentials;

  constructor() { }

  setCredentials(username: string, password: string) {
    this.credentials = new Credentials(username, password)
  }

  getCredentialsForApi() : Credentials {
    return this.credentials
  }
}
