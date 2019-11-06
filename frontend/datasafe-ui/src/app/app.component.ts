import {Component, OnInit} from '@angular/core';
import {ErrorStateMatcher} from "@angular/material";
import {FormControl, FormGroupDirective, NgForm} from "@angular/forms";

export class Env {

    // Read environment variables from browser window
    private static browserWindow = window || {};
    private static browserWindowEnv = Env.browserWindow['__env'] || {};

    static get(key): string {
        if (this.browserWindowEnv.hasOwnProperty(key)) {
            return window['__env'][key];
        }

        return null;
    }

    static apiUrl = Env.get('apiUrl');
    static apiUsername = Env.get('apiUsername');
    static apiPassword = Env.get('apiPassword');
}

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

export class ErrorMessageUtil {

    static extract(error): string {
        let errMsg = "Failed " + error.message;
        if (error && error.error && error.error.message) {
            errMsg = error.error.message;
        }

        return errMsg.substring(0, 32) + (errMsg.length >= 32 ? "..." : "");
    }
}

@Component({
    selector: 'app',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

    constructor() { }

    ngOnInit() {
    }
}
