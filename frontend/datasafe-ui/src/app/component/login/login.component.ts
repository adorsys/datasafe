import {Component, Inject, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {FormBuilder, FormControl, Validators} from '@angular/forms';
import {CredentialsService} from '../../service/credentials/credentials.service';
import {Env, FieldErrorStateMatcher} from '../../app.component';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';

export interface ApiConfigData {
  apiUrl: string;
  username: string;
  password: string;
}

@Component({
  selector: 'configure-api',
  templateUrl: 'configure.api.html',
  styleUrls: ['configure.api.css'],
})
export class ConfigureApiDialog {

  constructor(
      public dialogRef: MatDialogRef<ConfigureApiDialog>,
      @Inject(MAT_DIALOG_DATA) public data: ApiConfigData) {}

  onNoClick(): void {
    this.dialogRef.close();
  }
}

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  hide = true;

  userNameControl = new FormControl('', [
    Validators.required
  ]);

  passwordControl = new FormControl('', [
    Validators.required
  ]);

  loginForm = this.fb.group({
    username: this.userNameControl,
    passwords: this.passwordControl,
  });

  fieldMatcher = new FieldErrorStateMatcher();

  constructor(private router: Router, private fb: FormBuilder, private creds: CredentialsService, private dialog: MatDialog) { }

  ngOnInit() {
  }

  handleLoginClick() {
    if (!this.loginForm.valid) {
      return
    }

    this.creds.setCredentials(this.userNameControl.value, this.passwordControl.value);
    this.router.navigate(['/user']);
  }

  handleRegisterClick() {
    this.router.navigate(['/register']);
  }

  setupApiUrlAndCreds() {
    const dialogRef = this.dialog.open(ConfigureApiDialog, {
      width: '250px',
      data: {apiUrl: Env.apiUrl, username: Env.apiUsername, password: Env.apiPassword}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined) {
        Env.apiUrl = result.apiUrl;
        Env.apiUsername = result.username;
        Env.apiPassword = result.password;
      }
    });
  }
}
