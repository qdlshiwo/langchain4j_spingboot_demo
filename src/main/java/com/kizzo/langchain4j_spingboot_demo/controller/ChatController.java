package com.kizzo.langchain4j_spingboot_demo.controller;

import com.kizzo.langchain4j_spingboot_demo.config.AiConfig;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
@Slf4j
public class ChatController {

    @Autowired
    QwenChatModel qwenChatModel;

    @Autowired
    QwenStreamingChatModel qwenStreamingChatModel;

    @Autowired
    AiConfig.Assistant assistant;

    @Autowired
    AiConfig.AssistantUnique assistantUnique;

    @Autowired
    AiConfig.AssistantUnique assistantUniqueStore;

    @RequestMapping("/chat_qwen")
    public String chat(@RequestParam(defaultValue="你是谁") String message){
        String chat = qwenChatModel.chat(message);
        return chat;
    }

    @RequestMapping(value = "/stream_qwen",produces = "text/stream;charset=UTF8")
    public Flux<String> stream(@RequestParam(defaultValue="你是谁") String message){
        Flux<String> flux = Flux.create(fluxSink -> {

            qwenStreamingChatModel.chat(message, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String s) {
                    log.info(s);
                    fluxSink.next(s);
                }

                @Override
                public void onCompleteResponse(ChatResponse chatResponse) {
                    log.info(chatResponse.toString());
                    fluxSink.complete();
                }

                @Override
                public void onError(Throwable throwable) {
                    log.error(throwable.getMessage());
                    fluxSink.error(throwable);
                }
            });
        });
        return flux;
    }

    // 记忆普通对话
    @RequestMapping(value = "/memory_chat")
    public String memoryChat(@RequestParam(defaultValue="我是kizzo") String message){
        return assistant.chat(message);
    }

    // 记忆流对话
    @RequestMapping(value = "/memory_chat_stream",produces = "text/stream;charset=UTF8")
    public Flux<String> memoryStreamChat(@RequestParam(defaultValue="我是谁") String message) {
        TokenStream stream = assistant.stream(message);
        return Flux.create(sink ->  {
            stream.onPartialResponse(s -> sink.next(s))
                    .onCompleteResponse(c -> sink.complete())
                    .onError(sink::error)
                    .start();
        });
    }

    // 记忆隔离对话
    @RequestMapping(value = "/memoryId_chat")
    public String memoryIdChat(@RequestParam(defaultValue="我是kizzo") String message,Integer userId){
        return assistantUnique.chat(userId,message);
    }

    /**
     * 带 memoryId 的记忆对话接口（使用数据库持久化）
     */
    @RequestMapping("/memory_id_chat_store")
    public String memoryIdChatWithStore(@RequestParam("message") String message,
                                        @RequestParam("userId") Integer userId) {
        return assistantUniqueStore.chat(userId, message);
    }
    /**
     * 带 memoryId 的流式记忆对话接口（使用数据库持久化）
     */
    @RequestMapping(value = "/memory_id_chat_store_stream", produces = "text/stream;charset=UTF-8")
    public Flux<String> memoryIdChatWithStoreStream(@RequestParam("message") String message,
                                                    @RequestParam("userId") Integer userId) {
        TokenStream stream = assistantUniqueStore.stream(userId, message);
        return Flux.create(sink -> {
            stream.onPartialResponse(s -> sink.next(s))
                    .onCompleteResponse(c -> sink.complete())
                    .onError(sink::error)
                    .start();
        });
    }
}
