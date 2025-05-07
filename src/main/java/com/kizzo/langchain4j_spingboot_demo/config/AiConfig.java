package com.kizzo.langchain4j_spingboot_demo.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
    public interface Assistant{
        String chat(String message);
        // 流式响应
        TokenStream stream(String message);
    }
    public interface AssistantUnique{
        String chat(@MemoryId int memoryId, @UserMessage String message);
        // 流式响应
        TokenStream stream(@MemoryId int memoryId, @UserMessage String message);
    }

    @Bean
    public Assistant assistant(ChatLanguageModel chatLanguageModel, StreamingChatLanguageModel streamingChatLanguageModel){
        // 最多存储多少聊天记录
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        // 为Assistant动态代理对象chat ---> 对话内容存储ChatMemoryi ---> 聊天记录ChatMemory取出来 ---->放入到当前对话中
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemory(chatMemory)
                .build();
        return assistant;
    }

    @Bean
    public AssistantUnique assistantUnique(ChatLanguageModel chatLanguageModel,
                                           StreamingChatLanguageModel streamingChatLanguageModel){
        AssistantUnique assistant = AiServices.builder(AssistantUnique.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                // chatMemory变为了chatMemoryProvider，让memoryId与聊天记录绑定并作为Map的key
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder().maxMessages(10).id(memoryId).build() )
                .build();
        return assistant;
    }

    @Bean
    public AssistantUnique assistantUniqueStore(ChatLanguageModel chatLanguageModel,
                                                StreamingChatLanguageModel streamingChatLanguageModel,
                                                PersistentChatMemoryStore store){
        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory
                .builder()
                // 这个设置只会影响内存中的 MessageWindowChatMemory 实例，并不会自动限制写入数据库的数据量
                .maxMessages(10)
                .chatMemoryStore(store)
                .id(memoryId)
                .build();

        AssistantUnique assistant = AiServices.builder(AssistantUnique.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                // chatMemory变为了chatMemoryProvider，让memoryId与聊天记录绑定并作为Map的key
                .chatMemoryProvider(chatMemoryProvider)
                .build();
        return assistant;
    }

}
