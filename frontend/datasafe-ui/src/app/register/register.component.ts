import {Component, OnInit} from '@angular/core';
import {ApiService} from "../api.service";
import {
    AbstractControl,
    FormBuilder,
    FormControl,
    FormGroup,
    FormGroupDirective,
    NgForm,
    Validators
} from "@angular/forms";
import {ErrorStateMatcher} from "@angular/material";

export class FieldErrorStateMatcher implements ErrorStateMatcher {
    isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
        const isSubmitted = form && form.submitted;
        return !!(control && control.invalid && (control.dirty || control.touched || isSubmitted));
    }
}

export class ParentOrFieldErrorStateMatcher implements ErrorStateMatcher {
    isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
        const invalidCtrl = !!(control && control.invalid && control.parent.dirty);
        const invalidParent = !!(control && control.parent && control.parent.invalid && control.parent.dirty);

        return (invalidCtrl || invalidParent);
    }
}

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

    constructor(private api: ApiService, private fb: FormBuilder) {
    }

    ngOnInit() {
    }

    public handleCreateUserClick() {
        if (!this.registerForm.valid) {
            return
        }

        this.api.createUser(this.userNameControl.value, this.passwordControl.value);
    }

    private static checkPasswords(group: FormGroup) { // here we have the 'passwords' group
        let pass = group.controls.passwords.value;
        let confirmPass = group.controls.matchPasswords.value;

        return pass === confirmPass ? null : { notSame: true }
    }
}
