import { inject, Injectable, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { MessageSend } from '../models/message-send';
import { Message } from '../models/message';
import { AuthService } from './auth-service';

@Injectable({
  providedIn: 'root',
})
export class MessageService {
  private readonly authService = inject(AuthService);

  private readonly messages = signal<Message[]>([]);
  msgs = this.messages.asReadonly();

  private client: Client | null = null;

  connect() {
    this.disconnect();

    this.client = new Client({
      brokerURL: 'ws://localhost:8080/chat/api/ws',
      onConnect: () => {
        this.client?.subscribe('/topic/messages', (res) => {
          console.log(`Received: ${res.body}`);
          if (res.body) {
            const message = JSON.parse(res.body);
            this.messages.set([...this.messages(), message]);
          }
        });
      },
      connectHeaders: {
        Authorization: 'Bearer ' + this.authService.getToken(),
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
}
