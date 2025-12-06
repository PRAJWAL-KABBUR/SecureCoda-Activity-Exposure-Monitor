package com.securecoda.model;
import lombok.Data;
import javax.persistence.*;
import java.time.Instant;
@Entity
@Data
public class AlertEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String docId;
    private String docName;
    private String type;
    @Column(length=4000) private String details;
    private Instant detectedAt = Instant.now();
    private boolean resolved = false;
    private String rowId;
}
