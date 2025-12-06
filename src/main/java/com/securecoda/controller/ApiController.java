package com.securecoda.controller;
import com.securecoda.model.AlertEntity;
import com.securecoda.model.DocumentEntity;
import com.securecoda.repo.AlertRepository;
import com.securecoda.repo.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {
    private final AlertRepository alertRepo;
    private final DocumentRepository docRepo;
    @GetMapping("/alerts")
    public Page<AlertEntity> alerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return alertRepo.findByResolvedFalse(PageRequest.of(page, size));
    }
    @GetMapping("/alerts/unresolved") public List<AlertEntity> unresolvedAlerts(){ return alertRepo.findByResolvedFalse(); }
    @GetMapping("/docs")
    public Page<DocumentEntity> docs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return docRepo.findAll(PageRequest.of(page, size));
    }

    @PostMapping("/alerts/{id}/resolve")
    public AlertEntity resolve(@PathVariable Long id){
        AlertEntity a = alertRepo.findById(id).orElseThrow();
        a.setResolved(true);
        return alertRepo.save(a);
    }
    @PostMapping("/remediate/delete-doc/{docId}") public String deleteDoc(@PathVariable String docId){ return "disabled in sample"; }
    @PostMapping("/remediate/remove-publish/{docId}") public String removePublish(@PathVariable String docId){ return "disabled in sample"; }
}
