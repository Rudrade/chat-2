import {
  Component,
  inject,
  input,
  OnDestroy,
  AfterViewInit,
  ViewChild,
  ElementRef,
  signal,
  computed,
  effect,
} from '@angular/core';
import { MessageSummary } from '../../models/message-summary';
import { MessageService } from '../../services/message-service';
import { FormsModule } from '@angular/forms';
import { AvatarModule } from 'primeng/avatar';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TooltipModule } from 'primeng/tooltip';
import { CardModule } from 'primeng/card';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-chat',
  imports: [
    FormsModule,
    AvatarModule,
    InputGroupModule,
    InputGroupAddonModule,
    ButtonModule,
    InputTextModule,
    TooltipModule,
    CardModule,
  ],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class Chat implements OnDestroy, AfterViewInit {
  private readonly authService = inject(AuthService);
  private readonly messageService = inject(MessageService);
  messages = this.messageService.msgs;
  private readonly messagesCount = this.messageService.msgsCount;

  // paging / infinite-scroll state
  private pageNumber = 1;
  private isLoadingMore = false;
  private canLoadMore = true;
  private oldScrollHeight = 0;
  private messageCountBefore = 0;
  displayedMessages = computed(() => this.messages());

  @ViewChild('msgContainer') msgContainer!: ElementRef<HTMLDivElement>;
  private lastMessagesLen = 0;

  data = input.required<MessageSummary | undefined>();
  message = signal<string | null>(null);

  readonly loggedId = this.authService.getSubject();

  constructor() {
    effect(() => {
      const all = this.messages();
      const len = all.length;
      if (!this.msgContainer) {
        this.lastMessagesLen = len;
        return;
      }

      const el = this.msgContainer.nativeElement;

      // Handle new messages from other users (scroll to bottom)
      if (len > this.lastMessagesLen && !this.isLoadingMore) {
        setTimeout(() => (el.scrollTop = el.scrollHeight), 0);
      }

      // Handle loading older messages (adjust scroll position)
      if (this.isLoadingMore && len > this.messageCountBefore) {
        const newScrollHeight = el.scrollHeight;
        const heightDifference = newScrollHeight - this.oldScrollHeight;
        setTimeout(() => (el.scrollTop = heightDifference), 0);
        this.isLoadingMore = false;
      }

      // Check if we've reached the beginning
      if (this.isLoadingMore && len === this.messageCountBefore) {
        this.canLoadMore = false;
        this.isLoadingMore = false;
      }

      this.lastMessagesLen = len;
    });
  }

  ngOnDestroy(): void {
    this.messageService.disconnect();
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.scrollToBottom(), 0);
  }

  private scrollToBottom() {
    const el = this.msgContainer?.nativeElement;
    if (el) el.scrollTop = el.scrollHeight;
  }

  onScroll(_event: Event) {
    const el = this.msgContainer?.nativeElement;
    if (!el) return;

    if (el.scrollTop < 50 && !this.isLoadingMore && this.canLoadMore) {
      this.loadMoreOlder();
    }
  }
  // TODO: When click on chat already open, messages are getting duplicated and not cleared
  // TODO: After a while, sending messages stop working: dev.rudrade.chat.exception.InvalidDataException: cannot send message to yourself

  private loadMoreOlder() {
    const el = this.msgContainer?.nativeElement;
    if (!el || this.isLoadingMore) return;

    this.isLoadingMore = true;
    this.oldScrollHeight = el.scrollHeight;
    this.messageCountBefore = this.messages().length;

    // Load older messages
    this.messageService.getMessages(this.pageNumber, this.messageService.pageSize);
    this.pageNumber++;
  }

  onMessageSend() {
    if (!this.message()) return;

    this.messageService.sendChat(this.message()!, this.data()?.userId, this.data()?.chatId);
    this.message.set(null);
  }

  onKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      this.onMessageSend();
    }
  }
}
