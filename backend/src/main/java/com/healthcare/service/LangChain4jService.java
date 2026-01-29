package com.healthcare.service;

import com.healthcare.entity.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LangChain4jService {

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-3.5-turbo}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.base-url:https://api.openai.com}")
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

    private String mockReply(String userInput) {
        return "【模拟回复】您描述的情况已收到。建议您注意休息、多喝水，如症状加重或持续未缓解，请及时到线下医疗机构就诊。本回复仅作演示，未使用真实 AI 模型。";
    }
}
