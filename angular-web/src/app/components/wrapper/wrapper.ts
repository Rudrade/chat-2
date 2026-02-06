import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-wrapper',
  imports: [RouterOutlet],
  templateUrl: './wrapper.html',
  styleUrl: './wrapper.css',
})
export class Wrapper {}
