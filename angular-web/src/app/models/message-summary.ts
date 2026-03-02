export interface MessageSummary {
  chatId?: string;
  userId?: string;
  name: string;
  online: boolean;
  lastMessage: string;
  dtSent: Date;
  image: string;
}
