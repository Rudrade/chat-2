import { Component, output, signal } from '@angular/core';
import { UserMessageItem } from '../../models/user-message-item';
import { UserMessagesItem } from './user-messages-item/user-messages-item';

@Component({
  selector: 'app-user-messages',
  imports: [UserMessagesItem],
  templateUrl: './user-messages.html',
  styleUrl: './user-messages.css',
})
export class UserMessages {
  messages = signal<UserMessageItem[]>([]);

  setChat = output<UserMessageItem>();

  onUserClick(chat: UserMessageItem) {
    this.setChat.emit(chat);
  }
}
