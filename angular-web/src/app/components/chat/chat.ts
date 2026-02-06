import { Component, inject, input, OnDestroy, signal } from '@angular/core';
import { UserMessageItem } from '../../models/user-message-item';
import { MessageService } from '../../services/message-service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-chat',
  imports: [FormsModule],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class Chat implements OnDestroy {
  private readonly messageService = inject(MessageService);
  messages = this.messageService.msgs;

  data = input.required<UserMessageItem | undefined>();
  message = signal<string | null>(null);

  ngOnDestroy(): void {
    this.messageService.disconnect();
  }

  onMessageSend() {
    if (!this.message()) return;

    this.messageService.sendChat(this.message()!);
    this.message.set(null);
  }
}
