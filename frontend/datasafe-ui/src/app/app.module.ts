import './polyfills';

import {HttpClientModule} from '@angular/common/http';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatNativeDateModule} from '@angular/material/core';
import {BrowserModule} from '@angular/platform-browser';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {DemoMaterialModule} from './material-module';
import {RouterModule, Routes} from '@angular/router';

import {UserComponent} from './component/user/user.component';
import {ConfigureApiDialog, LoginComponent} from './component/login/login.component';
import {RegisterComponent} from './component/register/register.component';
import {AddFolderDialog, FiletreeComponent} from './component/filetree/filetree.component';
import {AppComponent} from "./app.component";

const appRoutes: Routes = [
  {path: '', component: LoginComponent},
  {path: 'login', component: LoginComponent},
  {path: 'register', component: RegisterComponent},
  {path: 'user', component: UserComponent}
];

@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    HttpClientModule,
    DemoMaterialModule,
    MatNativeDateModule,
    ReactiveFormsModule,
    RouterModule.forRoot(appRoutes)
  ],
  entryComponents: [AppComponent, AddFolderDialog, ConfigureApiDialog],
  declarations: [AppComponent, UserComponent, LoginComponent, RegisterComponent, FiletreeComponent, AddFolderDialog, ConfigureApiDialog],
  bootstrap: [AppComponent],
  providers: []
})
export class AppModule {}

platformBrowserDynamic().bootstrapModule(AppModule);
