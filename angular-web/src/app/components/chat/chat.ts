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
import { UserMessageItem } from '../../models/user-message-item';
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

  // paging / infinite-scroll state
  private readonly pageSize = 20;
  showCount = signal<number>(this.pageSize);
  displayedMessages = computed(() => {
    const all = this.messages();
    const start = Math.max(0, all.length - this.showCount()); // TODO: Make sure is order by dt sent
    return all.slice(start);
  });

  @ViewChild('msgContainer') msgContainer!: ElementRef<HTMLDivElement>;
  private lastMessagesLen = 0;

  data = input.required<UserMessageItem | undefined>();
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
      if (len > this.lastMessagesLen) {
        setTimeout(() => (el.scrollTop = el.scrollHeight), 0);
      }

      this.lastMessagesLen = len;
    });
  }

  scrolled = computed(() => {
    this.showCount();
    const el = this.msgContainer?.nativeElement;
    console.log('el:', el);
    console.log('scrolltop:', el?.scrollTop);
    console.log('scrollHeight:', el?.scrollHeight);
    return el?.scrollTop !== el?.scrollHeight;
  });

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

    if (el.scrollTop < 50 && this.showCount() < this.messages().length) {
      this.loadMoreOlder();
    }
  }

  private loadMoreOlder() {
    const el = this.msgContainer?.nativeElement;
    if (!el) return;

    const oldScrollHeight = el.scrollHeight;
    const oldScrollTop = el.scrollTop;

    const next = Math.min(this.messages().length, this.showCount() + this.pageSize);
    this.showCount.set(next);

    setTimeout(() => {
      const newScrollHeight = el.scrollHeight;
      el.scrollTop = newScrollHeight - oldScrollHeight + oldScrollTop;
    }, 0);
  }

  onMessageSend() {
    if (!this.message()) return;

    this.messageService.sendChat(this.message()!);
    this.message.set(null);
  }

  onKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      this.onMessageSend();
    }
  }
}
