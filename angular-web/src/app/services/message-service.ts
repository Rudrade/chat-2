import { inject, Injectable, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { MessageSend } from '../models/message-send';
import { Message } from '../models/message';
import { AuthService } from './auth-service';
import { MessageSummary } from '../models/message-summary';

@Injectable({
  providedIn: 'root',
})
export class MessageService {
  private readonly authService = inject(AuthService);

  private readonly messages = signal<Message[]>([]);
  msgs = this.messages.asReadonly();

  private readonly userMessages = signal<MessageSummary[]>([]);
  userMsgs = this.userMessages.asReadonly();

  private client: Client | null = null;

  connect() {
    if (this.isClientActive()) return;

    this.disconnect();

    this.messages.set([]);

    this.client = new Client({
      brokerURL: 'ws://localhost:8080/chat/api/ws',
      connectHeaders: {
        Authorization: this.authService.getToken()!,
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

  sendChat(message: string, idUserTo?: string, idChatTo?: string) {
    const payload: MessageSend = {
      text: message,
      idUserTo,
      idChatTo,
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
