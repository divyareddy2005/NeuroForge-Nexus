import 'zone.js';
import { provideZoneChangeDetection } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { AppComponent } from './app';
import { auth } from './auth';

// Keycloak hosts the login and registration pages; no passwords are handled by Angular.
auth.init({ onLoad: 'login-required', pkceMethod: 'S256', checkLoginIframe: false })
  .then(() => bootstrapApplication(AppComponent, { providers: [provideHttpClient(), provideZoneChangeDetection()] }))
  .catch(error => { console.error('Keycloak could not start', error); document.body.innerText = 'Cannot connect to Keycloak. Check Docker at http://localhost:8180'; });
