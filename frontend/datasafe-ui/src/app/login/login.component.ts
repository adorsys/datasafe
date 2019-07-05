import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {CredentialsService} from "../credentials.service";
import {FieldErrorStateMatcher} from "../app.component";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  private hide = true;

  private userNameControl = new FormControl('', [
    Validators.required
  ]);

  private passwordControl = new FormControl('', [
    Validators.required
  ]);

  private loginForm = this.fb.group({
    username: this.userNameControl,
    passwords: this.passwordControl,
  });

  private fieldMatcher = new FieldErrorStateMatcher();

  constructor(private router: Router, private fb: FormBuilder, private creds: CredentialsService) { }

  ngOnInit() {
  }

  public handleLoginClick() {
    if (!this.loginForm.valid) {
      return
    }

    this.creds.setCredentials(this.userNameControl.value, this.passwordControl.value);
    this.router.navigate(['/user']);
  }
}
