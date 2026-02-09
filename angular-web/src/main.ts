import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { Wrapper } from './app/components/wrapper/wrapper';

bootstrapApplication(Wrapper, appConfig).catch((err) => console.error(err));
