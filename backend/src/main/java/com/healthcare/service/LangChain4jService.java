package com.healthcare.service;

import com.healthcare.entity.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
@Slf4j
public class LangChain4jService {

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.base-url:}")
    private String baseUrl;

    public String chat(String systemPrompt, List<AiMessage> history, String userInput) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OpenAI API key not set, returning mock reply.");
            return mockReply(userInput);
        }
        try {
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .modelName(modelName)
                    .timeout(Duration.ofSeconds(60))
                    .build();
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(SystemMessage.from(systemPrompt));
            for (AiMessage m : history) {
                if (m.getSender() == AiMessage.Sender.USER) {
                    messages.add(UserMessage.from(m.getContent()));
                } else {
                    messages.add(dev.langchain4j.data.message.AiMessage.from(m.getContent()));
                }
            }
            messages.add(UserMessage.from(userInput));
            return model.generate(messages).content().text();
        } catch (Exception e) {
            log.error("LangChain4j chat error", e);
            return "抱歉，AI 服务暂时不可用，请稍后再试。";
        }
    }

    /**
     * 流式输出（partialResponse 会多次回调）。
     * 注意：是否能真正流式，取决于你的 API 是否支持 OpenAI 的 stream 模式。
     */
    public void chatStream(String systemPrompt,
                           List<AiMessage> history,
                           String userInput,
                           Consumer<String> onPartial,
                           Consumer<String> onComplete,
                           Consumer<Throwable> onError) {
        if (apiKey == null || apiKey.isBlank()) {
            String reply = mockReply(userInput);
            onPartial.accept(reply);
            onComplete.accept(reply);
            return;
        }
        try {
            StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .modelName(modelName)
                    .timeout(Duration.ofSeconds(60))
                    .build();

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(SystemMessage.from(systemPrompt));
            for (AiMessage m : history) {
                if (m.getSender() == AiMessage.Sender.USER) {
                    messages.add(UserMessage.from(m.getContent()));
                } else {
                    messages.add(dev.langchain4j.data.message.AiMessage.from(m.getContent()));
                }
            }
            messages.add(UserMessage.from(userInput));

            StringBuilder acc = new StringBuilder();
            model.generate(messages, new StreamingResponseHandler<dev.langchain4j.data.message.AiMessage>() {
                @Override
                public void onNext(String token) {
                    acc.append(token);
                    onPartial.accept(token);
                }

                @Override
                public void onComplete(Response<dev.langchain4j.data.message.AiMessage> response) {
                    String full = response != null && response.content() != null ? response.content().text() : acc.toString();
                    onComplete.accept(full);
                }

                @Override
                public void onError(Throwable error) {
                    onError.accept(error);
                }
            });
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    private String mockReply(String userInput) {
        return "【模拟回复】您描述的情况已收到。建议您注意休息、多喝水，如症状加重或持续未缓解，请及时到线下医疗机构就诊。本回复仅作演示，未使用真实 AI 模型。";
    }
}
