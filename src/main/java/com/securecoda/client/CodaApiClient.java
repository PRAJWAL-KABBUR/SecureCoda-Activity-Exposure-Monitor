package com.securecoda.client;
import com.securecoda.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CodaApiClient {
    private final WebClient codaWebClient;
    public Mono<CodaDocumentListResponse> listDocuments() {
        return codaWebClient.get().uri("/docs").retrieve().bodyToMono(CodaDocumentListResponse.class);
    }
    public Mono<CodaTablesResponse> listTables(String docId) {
        return codaWebClient.get().uri(uri -> uri.path("/docs/{docId}/tables").build(docId))
                .retrieve().bodyToMono(CodaTablesResponse.class);
    }
    public Mono<CodaRowsResponse> listRows(String docId, String tableId) {
        return codaWebClient.get().uri(uri -> uri.path("/docs/{docId}/tables/{tableId}/rows").build(docId, tableId))
                .retrieve().bodyToMono(CodaRowsResponse.class);
    }
    public Mono<CodaPagesResponse> listPages(String docId) {
        return codaWebClient.get().uri(uri -> uri.path("/docs/{docId}/pages").build(docId))
                .retrieve().bodyToMono(CodaPagesResponse.class);
    }
    public Mono<String> exportPageHtml(String docId, String pageId) {
        return codaWebClient.get().uri(uri -> uri.path("/docs/{docId}/pages/{pageId}/html").build(docId, pageId))
                .accept(MediaType.TEXT_HTML).retrieve().bodyToMono(String.class);
    }
    public Mono<CodaColumnsResponse> listColumns(String docId, String tableId) {
        return codaWebClient.get().uri(uri -> uri.path("/docs/{docId}/tables/{tableId}/columns").build(docId, tableId))
                .retrieve().bodyToMono(CodaColumnsResponse.class);
    }
    public Mono<Void> deleteDocument(String docId){ return codaWebClient.delete().uri(uri -> uri.path("/docs/{docId}").build(docId)).retrieve().bodyToMono(Void.class); }
    public Mono<Void> disablePublishing(String docId){ return codaWebClient.put().uri(uri -> uri.path("/docs/{docId}/publish").build(docId)).contentType(MediaType.APPLICATION_JSON).bodyValue("{\"enabled\":false}").retrieve().bodyToMono(Void.class); }
}
