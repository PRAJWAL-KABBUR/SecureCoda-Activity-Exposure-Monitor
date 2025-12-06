package com.securecoda.model;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
@Entity
@Data
public class DocumentEntity {
    @Id private String id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean published;
    private String browserLink;
}
