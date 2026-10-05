package mx.com.lab.spei.controller;

import jakarta.validation.Valid;
import mx.com.lab.spei.dto.SpeiInboundRequest;
import mx.com.lab.spei.dto.SpeiInboundResult;
import mx.com.lab.spei.dto.SpeiOrderResponse;
import mx.com.lab.spei.service.SpeiInboundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/spei")
public class SpeiInboundController {

    private final SpeiInboundService speiInboundService;

    public SpeiInboundController(SpeiInboundService speiInboundService) {
        this.speiInboundService = speiInboundService;
    }

    @PostMapping("/inbound")
    public ResponseEntity<SpeiOrderResponse> processInbound(
            @RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey,
            @Valid @RequestBody SpeiInboundRequest request) {

        SpeiInboundResult result = speiInboundService.process(request, idempotencyKey);
        if (result.isIdempotentReplay()) {
            return ResponseEntity.ok(result.response());
        }
        return ResponseEntity.status(201).body(result.response());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpeiOrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(speiInboundService.findById(id));
    }
}