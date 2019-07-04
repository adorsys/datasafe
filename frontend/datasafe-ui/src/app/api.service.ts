import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({providedIn: 'root'})
export class ApiService {

  public apiUserName = "root";
  public apiPassword = "root";

  private uri = "http://localhost:8080";
  private authorizeUri = this.uri + "/api/authenticate";
  private createUserUri = this.uri + "/api/authenticate";

  private token;

  constructor(private httpClient: HttpClient) { }

  public authorize(apiUsername: string, apiPassword: string){
    return this.httpClient.post(
        this.authorizeUri,
        {"userName": apiUsername, "password": apiPassword},
        {observe: 'response'}
    ).subscribe(res => {
      this.token = res.headers.get('token');
    });
  }

  public createUser(username: string, password: string){
    return this.httpClient.put(
        this.createUserUri,
        {"userName": username, "password": password},
        {observe: 'response'}
    ).subscribe(res => {
      this.token = res.headers.get('token');
    });
  }
}
