import { Component, input } from '@angular/core';
import { MessageSummary } from '../../../models/message-summary';
import { DatePipe } from '@angular/common';
import { AvatarModule } from 'primeng/avatar';
import { AvatarGroupModule } from 'primeng/avatargroup';

@Component({
  selector: 'app-user-messages-item',
  imports: [DatePipe, AvatarModule, AvatarGroupModule],
  templateUrl: './user-messages-item.html',
  styleUrl: './user-messages-item.css',
})
export class UserMessagesItem {
  data = input.required<MessageSummary>();
}
