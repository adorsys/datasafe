import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {CredentialsService} from "../../service/credentials/credentials.service";
import {ApiService} from "../../service/api/api.service";

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit {

  error: string;
  userName: string;

  constructor(private creds: CredentialsService, private api: ApiService, private router: Router) {
    this.userName = creds.getCredentialsForApi().username;
  }

  ngOnInit() {
    if (null == this.creds.getCredentialsForApi()) {
      this.router.navigate([''])
    }
  }

  doLogout() {
    this.router.navigate([''])
  }
}
