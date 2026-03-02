import { Component, inject, signal } from '@angular/core';
import { Chat } from './components/chat/chat';
import { UserMessages } from './components/user-messages/user-messages';
import { Navbar } from './components/navbar/navbar';
import { MessageSummary } from './models/message-summary';
import { MessageService } from './services/message-service';
import { AuthService } from './services/auth-service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
  imports: [Chat, UserMessages, Navbar],
})
export class App {
  readonly currentChat = signal<MessageSummary | undefined>(undefined);
  private readonly messageService = inject(MessageService);
  private readonly authService = inject(AuthService);
  username = this.authService.getCurrenUsername();

  setCurrentChat(currentChat: MessageSummary) {
    this.messageService.disconnect();
    this.currentChat.set(currentChat);
    this.messageService.setCurrentChat(currentChat.chatId);
  }
}
