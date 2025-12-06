package com.securecoda.dto;
import lombok.Data;
@Data
public class CodaDocument {
    private String id;
    private String name;
    private String href;
    private String browserLink;
    private String createdAt;
    private String updatedAt;
    private Publish publish;
    @Data public static class Publish { private Boolean enabled; private String url; private String visibility; }
}
