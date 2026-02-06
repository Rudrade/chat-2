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
  messages = signal<UserMessageItem[]>([
    {
      id: '12',
      name: 'Allen Barone',
      online: true,
      lastMessage: 'Hello!',
      time: new Date(),
      image:
        'https://cdn.dribbble.com/users/1944785/avatars/normal/c4f06458cb693b7d4a0ebb91199836ff.jpg?1592244019',
    },
    {
      id: '45',
      name: 'Juana Hill',
      online: false,
      lastMessage: 'what the fuck',
      time: new Date(),
      image:
        'https://cdn.dribbble.com/users/78433/avatars/small/9a635e75bcad74dbaed1d6b2614ebdc7.png?1762259990',
    },
  ]);

  setChat = output<UserMessageItem>();

  onUserClick(chat: UserMessageItem) {
    this.setChat.emit(chat);
  }
}
