import { Component, OnInit } from '@angular/core';
import {CredentialsService} from "../credentials.service";
import {ApiService} from "../api.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit {

  private error: string;

  constructor(private creds: CredentialsService, private api: ApiService, private router: Router) { }

  ngOnInit() {
    if (null == this.creds.getCredentialsForApi()) {
      this.router.navigate([''])
    }

    this.api.listDocuments("", this.creds.getCredentialsForApi())
        .then(data => console.log("Data: " + data))
        .catch(err => {
          if (err.code == 401 || err.code == 403) {
            this.router.navigate(['']);
            return;
          }
          this.error = err.error.message
        });
  }
}
