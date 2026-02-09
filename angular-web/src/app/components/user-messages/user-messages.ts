import { Component, output, signal } from '@angular/core';
import { UserMessageItem } from '../../models/user-message-item';
import { UserMessagesItem } from './user-messages-item/user-messages-item';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputText } from 'primeng/inputtext';
import { FloatLabelModule } from 'primeng/floatlabel';

@Component({
  selector: 'app-user-messages',
  imports: [UserMessagesItem, IconFieldModule, InputIconModule, InputText, FloatLabelModule],
  templateUrl: './user-messages.html',
  styleUrl: './user-messages.css',
})
export class UserMessages {
  messages = signal<UserMessageItem[]>([
    {
      id: 'test',
      name: 'John',
      online: false,
      lastMessage: 'Fuck this',
      time: new Date(),
      image: 'https://simons.berkeley.edu/sites/default/files/profiles/GuyHeadShot_3.jpg',
    },
    {
      id: '2',
      name: 'Smith',
      online: true,
      lastMessage: 'wut',
      time: new Date(),
      image:
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSAGJTMIhPARBvFdMCaZscF0LzK3s15-w6dgQ&s',
    },
  ]);

  setChat = output<UserMessageItem>();

  onUserClick(chat: UserMessageItem) {
    this.setChat.emit(chat);
  }
}
