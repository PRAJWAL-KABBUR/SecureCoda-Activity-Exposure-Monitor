package com.securecoda.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class SlackNotifier {
    private final WebClient webClient = WebClient.builder().baseUrl("https://hooks.slack.com").build();
    private final String webhook;
    public SlackNotifier(@Value("${slack.webhook.url:}") String webhook) { this.webhook = webhook; }
    public Mono<Void> send(String title, String body){
        if (webhook == null || webhook.isEmpty()) return Mono.empty();
        Map<String,Object> payload = Map.of("text", "*"+title+"*\n"+body);
        return webClient.post().uri(webhook.replace("https://hooks.slack.com",""))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload).retrieve().bodyToMono(Void.class);
    }
}
