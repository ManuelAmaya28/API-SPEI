package mx.com.lab.spei.service;

import mx.com.lab.spei.client.FineractClient;
import mx.com.lab.spei.client.dto.FineractAccountInfo;
import mx.com.lab.spei.client.dto.FineractDepositRequest;
import mx.com.lab.spei.config.SpeiProperties;
import mx.com.lab.spei.domain.SpeiOrder;
import mx.com.lab.spei.domain.SpeiOrderStatus;
import mx.com.lab.spei.dto.SpeiInboundRequest;
import mx.com.lab.spei.dto.SpeiInboundResult;
import mx.com.lab.spei.dto.SpeiOrderResponse;
import mx.com.lab.spei.exception.AccountNotFoundException;
import mx.com.lab.spei.repository.SpeiOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpeiInboundServiceImplTest {

    @Mock FineractClient fineractClient;
    @Mock IdempotencyService idempotencyService;
    @Mock SpeiOrderRepository speiOrderRepository;

    private SpeiInboundServiceImpl service;

    @BeforeEach
    void setUp() {
        SpeiProperties props = new SpeiProperties();
        props.setSpreadRate(new BigDecimal("0.005"));
        props.setCommission(new BigDecimal("8.00"));
        props.setPaymentTypeId(3);
        SpeiProperties.Fineract f = new SpeiProperties.Fineract();
        f.setBaseUrl("http://localhost");
        f.setUsername("mifos");
        f.setPassword("password");
        f.setTenantId("default");
        props.setFineract(f);
        service = new SpeiInboundServiceImpl(fineractClient, new AmountCalculator(),
                idempotencyService, speiOrderRepository, props);
    }

    @Test
    void process_happyPath_registersThreeDepositsWithCorrectNotes() {
        SpeiInboundRequest req = new SpeiInboundRequest(
                "SPEI20261001120000", new BigDecimal("1000.00"), "646180909697341557", "Test");

        when(idempotencyService.checkAndLock(null)).thenReturn(Optional.empty());
        when(fineractClient.findAccountByClabe("646180909697341557")).thenReturn(
                FineractAccountInfo.builder().savingsId(12L).clientId(7L).clientName("Cliente SPEI Lab").build());
        when(fineractClient.depositTransaction(eq(12L), any())).thenReturn(145L, 146L, 147L);

        SpeiOrder saved = buildSavedOrder();
        when(speiOrderRepository.save(any())).thenReturn(saved);

        SpeiInboundResult result = service.process(req, null);

        assertFalse(result.isIdempotentReplay());
        assertEquals("APPLIED", result.response().status());
        assertEquals(0, new BigDecimal("1000.00").compareTo(result.response().principal()));
        assertEquals(0, new BigDecimal("5.00").compareTo(result.response().spread()));
        assertEquals(0, new BigDecimal("8.00").compareTo(result.response().commission()));

        ArgumentCaptor<FineractDepositRequest> captor = ArgumentCaptor.forClass(FineractDepositRequest.class);
        verify(fineractClient, times(3)).depositTransaction(eq(12L), captor.capture());
        List<FineractDepositRequest> calls = captor.getAllValues();
        assertEquals("SPEI-IN principal SPEI20261001120000", calls.get(0).getNote());
        assertEquals("SPEI-IN spread SPEI20261001120000",    calls.get(1).getNote());
        assertEquals("SPEI-IN commission SPEI20261001120000", calls.get(2).getNote());
    }

    @Test
    void process_idempotentKey_returnsCachedResult_withoutFineract() {
        SpeiInboundRequest req = new SpeiInboundRequest(
                "SPEI20261001120000", new BigDecimal("1000.00"), "646180909697341557", null);
        UUID key = UUID.randomUUID();

        SpeiOrderResponse cached = new SpeiOrderResponse(UUID.randomUUID(), "SPEI20261001120000",
                "646180909697341557", 7L, "Cliente", 12L,
                new BigDecimal("1000.00"), new BigDecimal("5.00"), new BigDecimal("8.00"),
                145L, 146L, 147L, Instant.now(), "APPLIED");
        when(idempotencyService.checkAndLock(key)).thenReturn(Optional.of(cached));

        SpeiInboundResult result = service.process(req, key);

        assertTrue(result.isIdempotentReplay());
        assertEquals(cached, result.response());
        verifyNoInteractions(fineractClient);
    }

    @Test
    void process_accountNotFound_throwsException() {
        SpeiInboundRequest req = new SpeiInboundRequest(
                "SPEI20261001120000", new BigDecimal("1000.00"), "646180909697341557", null);
        when(idempotencyService.checkAndLock(null)).thenReturn(Optional.empty());
        when(fineractClient.findAccountByClabe(any()))
                .thenThrow(new AccountNotFoundException("646180909697341557"));

        assertThrows(AccountNotFoundException.class, () -> service.process(req, null));
    }

    @Test
    void findById_unknownId_throws404() {
        UUID id = UUID.randomUUID();
        when(speiOrderRepository.findById(id)).thenReturn(Optional.empty());
        var ex = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.findById(id));
        assertEquals(404, ex.getStatusCode().value());
    }

    private SpeiOrder buildSavedOrder() {
        SpeiOrder o = new SpeiOrder();
        o.setId(UUID.randomUUID());
        o.setClaveRastreo("SPEI20261001120000");
        o.setMonto(new BigDecimal("1000.00"));
        o.setCuentaBeneficiaria("646180909697341557");
        o.setPrincipal(new BigDecimal("1000.00"));
        o.setSpread(new BigDecimal("5.00"));
        o.setCommission(new BigDecimal("8.00"));
        o.setFineractClientId(7L);
        o.setFineractClientName("Cliente SPEI Lab");
        o.setFineractSavingsId(12L);
        o.setFineractDepositTxId(145L);
        o.setFineractSpreadTxId(146L);
        o.setFineractCommissionTxId(147L);
        o.setProcessedAt(Instant.now());
        o.setStatus(SpeiOrderStatus.APPLIED);
        return o;
    }
}