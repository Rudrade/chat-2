import { Component, input } from '@angular/core';
import { UserMessageItem } from '../../../models/user-message-item';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-user-messages-item',
  imports: [DatePipe],
  templateUrl: './user-messages-item.html',
  styleUrl: './user-messages-item.css',
})
export class UserMessagesItem {
  data = input.required<UserMessageItem>();
}
