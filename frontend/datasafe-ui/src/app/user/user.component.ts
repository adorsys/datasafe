import { Component, OnInit } from '@angular/core';
import {CredentialsService} from "../credentials.service";
import {ApiService} from "../api.service";

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit {

  constructor(private creds: CredentialsService, private api: ApiService) { }

  ngOnInit() {
    this.api.listDocuments("", this.creds.getCredentialsForApi())
        .then(data => console.log("Data: " + data))
        .catch(err => console.log("Error: " + err.message))
  }
}
