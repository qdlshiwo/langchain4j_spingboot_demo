package com.kizzo.langchain4j_spingboot_demo.config;

import com.kizzo.langchain4j_spingboot_demo.mapper.ChatMessageMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PersistentChatMemoryStore implements ChatMemoryStore {
    private final ChatMessageMapper chatMessageMapper;

    public PersistentChatMemoryStore(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String memoryIdStr = memoryId.toString();
        List<String> jsonMessages = chatMessageMapper.selectMessagesByMemoryId(memoryIdStr);

        return jsonMessages.stream()
                .map(ChatMessageDeserializer::messagesFromJson)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String memoryIdStr = memoryId.toString();
        String json = ChatMessageSerializer.messagesToJson(messages);
        chatMessageMapper.insertMessages(memoryIdStr, json);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        chatMessageMapper.deleteMessagesByMemoryId(memoryId.toString());
    }
}
