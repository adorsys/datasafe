import {Injectable} from '@angular/core';
import {HttpClient, HttpRequest, HttpResponse} from "@angular/common/http";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";

@Injectable({providedIn: 'root'})
export class ApiService {

    private static TOKEN_HEADER = "token";

    public apiUserName = "root";
    public apiPassword = "root";

    private uri = "http://localhost:8080";
    private authorizeUri = this.uri + "/api/authenticate";
    private createUserUri = this.uri + "/api/authenticate";

    private token: string;

    constructor(private httpClient: HttpClient) {
    }

    public authorize() {
        let result = this.httpClient.post(
            this.authorizeUri,
            {"userName": this.apiUserName, "password": this.apiPassword},
            {observe: 'response'}
        );

        result.subscribe(res => {
            this.token = ApiService.extractToken(res)
        });

        return result;
    }

    public createUser(username: string, password: string) {
        this.withAuthorization(
            token => this.httpClient.put(
                this.createUserUri,
                {"userName": username, "password": password},
                ApiService.headers(token)
                )
        )
    }

    private withAuthorization(call: (token: string) => any) : Observable<string> {
        if (null == this.token) {
            return this.authorize()
                .pipe(map(res => ApiService.extractToken(res)))
                .pipe(map(token => call(token)))
        }

        return of(this.token).pipe(map(token => call(token)))
    }

    private static headers(token: string) {
        return {"headers": {[ApiService.TOKEN_HEADER]: token}};
    }

    private static extractToken(response: HttpResponse<Object>) {
        return response.headers.get(ApiService.TOKEN_HEADER)
    }
}
