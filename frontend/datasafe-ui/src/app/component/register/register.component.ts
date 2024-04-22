import {Component} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {ApiService} from '../../service/api/api.service';
import {CredentialsService} from '../../service/credentials/credentials.service';
import {ErrorMessageUtil, FieldErrorStateMatcher, ParentOrFieldErrorStateMatcher} from '../../app.component';

class PasswordsMatchControl extends FormControl {

    constructor(private hidden: boolean) {
        super('', []);
    }

    get Hidden(): boolean {
        return this.hidden;
    }

    visible(): boolean {
        return !this.hidden;
    }

    set Hidden(hidden: boolean) {
        this.hidden = hidden;
    }
}

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.css']
})
export class RegisterComponent {

    constructor(public router: Router, private api: ApiService, private fb: FormBuilder,
                private creds: CredentialsService) {
    }

    userNameControl = new FormControl('', [
        Validators.required,
        Validators.minLength(3)
    ]);

    passwordControl = new FormControl('', [
        Validators.required,
        Validators.minLength(3)
    ]);

    passwordMatchControl = new PasswordsMatchControl(false);

    registerForm = this.fb.group({
        username: this.userNameControl,
        passwords: this.passwordControl,
        matchPasswords: this.passwordMatchControl
    }, {
        validators: [RegisterComponent.checkPasswords]
    });

    fieldMatcher = new FieldErrorStateMatcher();
    parentOrFieldMatcher = new ParentOrFieldErrorStateMatcher();

    private static checkPasswords(): ValidatorFn { // here we have the 'passwords' group
        return (group: FormGroup): ValidationErrors | null => {
            const matchControl = <PasswordsMatchControl>group.controls.matchPasswords;
            const pass = group.controls.passwords.value;
            const confirmPass = matchControl.value;

            return (matchControl.Hidden || pass === confirmPass) ? null : {notSame: true};
        };
    }

    public handleCreateUserClick() {
        if (!this.registerForm.valid) {
            return;
        }

        this.api.createUser(this.userNameControl.value, this.passwordControl.value)
            .then(() => {
                this.creds.setCredentials(this.userNameControl.value, this.passwordControl.value);
                this.router.navigate(['/user']);
            })
            .catch(error => this.registerForm.setErrors({'createFailed': ErrorMessageUtil.extract(error)}));
    }
}
