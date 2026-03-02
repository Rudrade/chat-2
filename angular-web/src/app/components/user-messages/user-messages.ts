import { Component, inject, OnInit, output, signal } from '@angular/core';
import { MessageSummary } from '../../models/message-summary';
import { UserMessagesItem } from './user-messages-item/user-messages-item';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputText } from 'primeng/inputtext';
import { FloatLabelModule } from 'primeng/floatlabel';
import { MessageService } from '../../services/message-service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-user-messages',
  imports: [
    UserMessagesItem,
    IconFieldModule,
    InputIconModule,
    InputText,
    FloatLabelModule,
    FormsModule,
  ],
  templateUrl: './user-messages.html',
  styleUrl: './user-messages.css',
})
export class UserMessages implements OnInit {
  private readonly messageService = inject(MessageService);
  messages = this.messageService.userMsgs;

  setChat = output<MessageSummary>();
  searchTerm = signal<string>('');

  ngOnInit(): void {
    console.log('usermessages - connecting');
    this.messageService.connect().subscribe({
      complete: () => {
        console.log('usermessages - complete');
        this.messageService.search(null);
      },
    });
  }

  onUserClick(chat: MessageSummary) {
    this.setChat.emit(chat);
  }

  onSearch(event: KeyboardEvent) {
    this.messageService.search(this.searchTerm());
  }
}
