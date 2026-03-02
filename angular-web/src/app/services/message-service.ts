import { inject, Injectable, signal } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { MessageSend } from '../models/message-send';
import { Message } from '../models/message';
import { AuthService } from './auth-service';
import { MessageSummary } from '../models/message-summary';
import { Observable, Subscription } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class MessageService {
  private readonly authService = inject(AuthService);
  private readonly currUser = this.authService.getSubject();

  private readonly messages = signal<Message[]>([]);
  msgs = this.messages.asReadonly();
  private readonly messageCount = signal<number>(0);
  msgsCount = this.messageCount.asReadonly();

  private readonly userMessages = signal<MessageSummary[]>([]);
  userMsgs = this.userMessages.asReadonly();

  private client: Client | null = null;
  private currentChat?: string;
  private connectSubscription?: Subscription;
  readonly pageSize = 40;

  //TODO after:toast on actions
  setCurrentChat(currentChat?: string) {
    console.log('changed to chat:', currentChat);

    // Unsubscribe from previous connection if switching chats
    if (this.connectSubscription) {
      this.connectSubscription.unsubscribe();
    }

    this.currentChat = currentChat;

    if (!this.currentChat) {
      return;
    }

    console.log('connecting in curr chat');
    this.connectSubscription = this.connect().subscribe({
      complete: () => {
        this.getMessages(0, this.pageSize);
      },
    });
  }

  getMessages(offset: number, limit: number) {
    const payload = {
      chatId: this.currentChat,
      offset,
      limit,
    };

    this.client?.publish({
      destination: `/app/messages`,
      body: JSON.stringify(payload),
      headers: { 'content-type': 'application/json' },
    });
  }

  connect() {
    return new Observable((subscriber) => {
      if (this.isClientActive()) {
        subscriber.complete();
        return;
      }

      try {
        this.messages.set([]);

        this.client = new Client({
          brokerURL: 'ws://localhost:8080/chat/api/ws',
          connectHeaders: {
            Authorization: this.authService.getToken()!,
          },
          onConnect: () => {
            this.client?.subscribe(`/user/topic/messages`, (res) => {
              if (res.body) {
                const resObj = JSON.parse(res.body);
                if (
                  !this.currentChat &&
                  resObj.count === 1 &&
                  resObj.messages[0].idFrom === this.currUser
                ) {
                  this.currentChat = resObj.messages[0].idChatTo;
                }

                // If messages array already has data, prepend (older messages go to top)
                // Otherwise append (initial load)
                const currentMessages = this.messages();
                if (currentMessages.length > 0) {
                  // Prepend: older messages at the beginning
                  this.messages.set([...resObj.messages, ...currentMessages]);
                } else {
                  // Initial load: append
                  this.messages.set([...resObj.messages]);
                }
                this.messageCount.set(resObj.count);
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

            subscriber.complete();
          },
          debug: (str) => {
            console.log(str);
          },
          reconnectDelay: 30000,
        });

        this.client.activate();
      } catch (err) {
        subscriber.error(err);
      }
    });
  }

  isClientActive() {
    return this.client?.active;
  }

  disconnect() {
    // if (this.isClientActive()) this.client?.deactivate();
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
    const payload = {
      filterType: term == null || term?.length == 0 ? 'ONLY_WITH_MESSAGES' : 'SEARCH',
      term,
    };

    this.client?.publish({
      destination: `/app/search`,
      body: JSON.stringify(payload),
      headers: { 'content-type': 'application/json' },
    });
  }
}
