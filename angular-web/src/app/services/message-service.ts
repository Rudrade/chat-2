import { inject, Injectable, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { MessageSend } from '../models/message-send';
import { Message } from '../models/message';
import { AuthService } from './auth-service';
import { UserMessageItem } from '../models/user-message-item';

@Injectable({
  providedIn: 'root',
})
export class MessageService {
  private readonly authService = inject(AuthService);
  private readonly userId = this.authService.getSubject();

  private readonly messages = signal<Message[]>([]);
  msgs = this.messages.asReadonly();

  private readonly userMessages = signal<UserMessageItem[]>([]);
  userMsgs = this.userMessages.asReadonly();

  private client: Client | null = null;

  connect() {
    if (this.isClientActive()) return;

    this.disconnect();

    this.messages.set([]);

    this.client = new Client({
      brokerURL: 'ws://localhost:8080/chat/api/ws',
      connectHeaders: {
        userId: this.userId!,
      },
      onConnect: () => {
        this.client?.subscribe(`/user/topic/messages`, (res) => {
          console.log(`Received: ${res.body}`);
          if (res.body) {
            const message = JSON.parse(res.body);
            this.messages.set([...this.messages(), message]);
          }
        });

        this.client?.subscribe('/user/topic/summaries', (res) => {
          if (res.body) {
            const result = JSON.parse(res.body);
            console.log('parsed message:', result);
            this.userMessages.set(result);
            console.log('userMsgs:', this.userMessages());
          }
        });
      },
      debug: (str) => {
        console.log(str);
      },
      reconnectDelay: 30000,
    });

    this.client.activate();
  }

  isClientActive() {
    return this.client?.active;
  }

  disconnect() {
    if (this.isClientActive()) this.client?.deactivate();
  }

  sendChat(message: string) {
    const payload: MessageSend = {
      idTo: '5405e343-c832-4972-a9e5-4702e9364ac2',
      text: message,
    };

    this.client?.publish({
      destination: '/app/sendMessage',
      body: JSON.stringify(payload),
      headers: { 'content-type': 'application/json' },
    });
  }

  search(term: string | null) {
    const param = term === null || term.length === 0 ? '__EMPTY__' : term;
    this.client?.publish({
      destination: `/app/search/${param}`,
    });
  }
}
