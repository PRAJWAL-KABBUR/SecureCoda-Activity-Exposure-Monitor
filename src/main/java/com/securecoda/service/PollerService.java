package com.securecoda.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.securecoda.client.CodaApiClient;
import com.securecoda.dto.*;
import com.securecoda.model.AlertEntity;
import com.securecoda.model.DocumentEntity;
import com.securecoda.repo.AlertRepository;
import com.securecoda.repo.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollerService {

    private final CodaApiClient coda;
    private final ScannerService scanner;
    private final AlertRepository alertRepo;
    private final DocumentRepository docRepo;
    private final SlackNotifier slack;
    private final ObjectMapper mapper = new ObjectMapper();

    @Scheduled(fixedDelayString = "${poller.interval.ms:60000}")
    public void poll() {

        log.info("Starting poll cycle");

        try {
            CodaDocumentListResponse docs = coda.listDocuments().block();
            if (docs == null || docs.getItems() == null) return;

            for (CodaDocument d : docs.getItems()) {

                // ====== FETCH OR CREATE DOCUMENT RECORD ======
                DocumentEntity ent = docRepo.findById(d.getId())
                        .orElseGet(DocumentEntity::new);

                ent.setId(d.getId());
                ent.setName(d.getName());

                try { ent.setCreatedAt(OffsetDateTime.parse(d.getCreatedAt()).toInstant()); }
                catch (Exception ex) { ent.setCreatedAt(Instant.now()); }

                try { ent.setUpdatedAt(OffsetDateTime.parse(d.getUpdatedAt()).toInstant()); }
                catch (Exception ex) { ent.setUpdatedAt(Instant.now()); }

                ent.setPublished(d.getPublish() != null &&
                        Boolean.TRUE.equals(d.getPublish().getEnabled()));
                ent.setBrowserLink(d.getBrowserLink());

                // Update only, no duplicate rows
                docRepo.save(ent);

                // ====== SCAN TABLES ======
                CodaTablesResponse tables = coda.listTables(d.getId()).block();
                if (tables == null || tables.getItems() == null) continue;

                for (CodaTable t : tables.getItems()) {

                    Map<String, String> colNames = new HashMap<>();
                    CodaColumnsResponse cols = coda.listColumns(d.getId(), t.getId()).block();

                    if (cols != null && cols.getItems() != null) {
                        for (CodaColumn c : cols.getItems()) {
                            colNames.put(c.getId(), c.getName());
                        }
                    }

                    CodaRowsResponse rows = coda.listRows(d.getId(), t.getId()).block();
                    if (rows == null || rows.getItems() == null) continue;

                    for (CodaRow r : rows.getItems()) {

                        List<ScannerService.Finding> findings =
                                scanner.scanRowValuesStructured(r.getValues(), colNames);

                        if (!findings.isEmpty()) {

                            // Build JSON details
                            ObjectNode root = mapper.createObjectNode();
                            root.put("table", t.getName());
                            ArrayNode arr = root.putArray("findings");

                            for (ScannerService.Finding f : findings) {
                                ObjectNode fn = arr.addObject();
                                fn.put("columnId", f.columnId);
                                fn.put("column", f.columnName);
                                fn.put("type", f.type);
                                fn.put("value", f.value);
                                fn.put("pattern", f.pattern);
                            }

                            String detailsJson = mapper
                                    .writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(root);

                            saveAlertIfNew(ent, r.getId(), detailsJson);
                        }
                    }
                }
            }

        } catch (Exception ex) {
            log.error("Poll error: {}", ex.getMessage(), ex);
        }

        log.info("Poll finished");
    }


    // 🚀 New dedupe alert logic
    private void saveAlertIfNew(DocumentEntity ent, String rowId, String detailsJson) {

        // CHECK if alert already exists for this doc-row and details
        boolean exists = alertRepo.existsByDocIdAndRowIdAndDetails(ent.getId(), rowId, detailsJson);

        if (exists) {
            log.info("Skipping duplicate alert for doc={} row={}", ent.getId(), rowId);
            return;
        }

        // CREATE NEW ALERT (first occurrence)
        AlertEntity a = new AlertEntity();
        a.setDocId(ent.getId());
        a.setDocName(ent.getName());
        a.setRowId(rowId);
        a.setType("SENSITIVE_DATA");
        a.setDetails(detailsJson);
        a.setDetectedAt(Instant.now());
        alertRepo.save(a);

        slack.send(
                "SecureCoda Alert: SENSITIVE_DATA",
                ent.getName() + "\n" + detailsJson
        ).onErrorResume(e -> Mono.empty()).subscribe();
    }
}
