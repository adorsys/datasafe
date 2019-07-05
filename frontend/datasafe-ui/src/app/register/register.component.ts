import {Component, OnInit} from '@angular/core';
import {ApiService} from "../api.service";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CredentialsService} from "../credentials.service";
import {FieldErrorStateMatcher, ParentOrFieldErrorStateMatcher} from "../app.component";

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

    private hide = true;

    private userNameControl = new FormControl('', [
        Validators.required,
        Validators.minLength(3)
    ]);

    private passwordControl = new FormControl('', [
        Validators.required,
        Validators.minLength(3)
    ]);

    private passwordMatchControl = new FormControl('', []);

    private registerForm = this.fb.group({
        username: this.userNameControl,
        passwords: this.passwordControl,
        matchPasswords: this.passwordMatchControl
    }, {validator: RegisterComponent.checkPasswords });

    private fieldMatcher = new FieldErrorStateMatcher();
    private parentOrFieldMatcher = new ParentOrFieldErrorStateMatcher();

    constructor(private router: Router, private api: ApiService, private fb: FormBuilder,
                private creds: CredentialsService) {
    }

    ngOnInit() {
    }

    public handleCreateUserClick() {
        if (!this.registerForm.valid) {
            return
        }

        this.api.createUser(this.userNameControl.value, this.passwordControl.value)
            .then(res => {
                this.creds.setCredentials(this.userNameControl.value, this.passwordControl.value);
                this.router.navigate(['/user'])
            })
            .catch(error => this.handleServerError(error))
    }

    private handleServerError(error) {
        this.registerForm.setErrors({
            'createFailed': error.error.message.substring(0, 32) + (error.error.message.length >= 32 ? "..." : "")
        })
    }

    private static checkPasswords(group: FormGroup) { // here we have the 'passwords' group
        let pass = group.controls.passwords.value;
        let confirmPass = group.controls.matchPasswords.value;

        return pass === confirmPass ? null : { notSame: true }
    }
}
