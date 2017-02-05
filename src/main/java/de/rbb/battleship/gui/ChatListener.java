package de.rbb.battleship.gui;

import java.util.ArrayList;
import java.util.List;

import de.rbb.battleship.gui.Chat.ChatStyle;

public interface ChatListener {

    void chatMessage(ChatMessage message);

    public class ChatMessage {
        private final String name;

        private final String text;

        private final ChatStyle chatStyle;

        public ChatMessage(ChatStyle chatStyle, String name, String text) {
            super();
            this.chatStyle = chatStyle;
            this.name = name;
            this.text = text;
        }

        public String getName() {
            return name;
        }

        public String getText() {
            return text;
        }

        public ChatStyle getChatStyle() {
            return chatStyle;
        }
    }

    public interface ChatBroadcaster {

        public abstract void addChatListener(ChatListener listener);

        public abstract void removeChatListener(ChatListener listener);

        public abstract void fireChatMessageEvent(ChatMessage message);

    }

    public abstract class DefaultChatBroadcaster implements ChatBroadcaster {

        private final List<ChatListener> chatListener;

        public DefaultChatBroadcaster() {
            chatListener = new ArrayList<ChatListener>();
        }

        public void addChatListener(ChatListener listener) {
            this.chatListener.add(listener);
        }

        public void removeChatListener(ChatListener listener) {
            this.chatListener.remove(listener);
        }

        public void fireChatMessageEvent(ChatMessage message) {
            for (ChatListener chatListener : this.chatListener) {
                chatListener.chatMessage(message);
            }
        }
    }
}
