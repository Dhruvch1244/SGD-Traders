import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import {HTTP_INTERCEPTORS, provideHttpClient} from '@angular/common/http';
import {AuthInterceptor} from './core/interceptors/auth.interceptor';
@NgModule({
  declarations: [],
  imports: [
    BrowserModule, // Includes CommonModule, so titlecase works
    ReactiveFormsModule,
    MatIconModule,
    FormsModule, // Needed for Material icons
  ],
  providers: [provideHttpClient(),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }],
  bootstrap: [],
})
export class AppModule {}
