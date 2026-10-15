package com.queueless.queue;

import com.queueless.common.response.ApiResponse;
import com.queueless.controller.CounterController;
import com.queueless.controller.QueueController;
import com.queueless.domain.entity.Branch;
import com.queueless.domain.entity.Counter;
import com.queueless.domain.entity.CounterServiceMapping;
import com.queueless.domain.entity.Token;
import com.queueless.domain.repository.BranchRepository;
import com.queueless.domain.repository.CounterRepository;
import com.queueless.domain.repository.CounterServiceMappingRepository;
import com.queueless.domain.repository.TokenRepository;
import com.queueless.dto.response.DisplayBoardResponse;
import com.queueless.dto.response.TokenResponse;
import com.queueless.security.GuestRateLimiter;
import com.queueless.security.GuestSessionService;
import com.queueless.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DisplayBoardAndCounterApiTest {

    private QueueEngine queueEngine;
    private TokenService tokenService;
    private ETAEngine etaEngine;
    private JwtTokenProvider tokenProvider;
    private GuestSessionService guestSessionService;
    private GuestRateLimiter guestRateLimiter;
    private TokenRepository tokenRepository;
    private BranchRepository branchRepository;
    private CounterRepository counterRepository;
    private CounterServiceMappingRepository csmRepository;
    private QueueBroadcaster queueBroadcaster;

    private QueueController queueController;
    private CounterController counterController;

    @BeforeEach
    void setUp() {
        queueEngine = mock(QueueEngine.class);
        tokenService = mock(TokenService.class);
        etaEngine = mock(ETAEngine.class);
        tokenProvider = mock(JwtTokenProvider.class);
        guestSessionService = mock(GuestSessionService.class);
        guestRateLimiter = mock(GuestRateLimiter.class);
        tokenRepository = mock(TokenRepository.class);
        branchRepository = mock(BranchRepository.class);
        counterRepository = mock(CounterRepository.class);
        csmRepository = mock(CounterServiceMappingRepository.class);
        queueBroadcaster = mock(QueueBroadcaster.class);

        queueController = new QueueController(
                queueEngine,
                tokenService,
                etaEngine,
                tokenProvider,
                guestSessionService,
                guestRateLimiter,
                tokenRepository,
                branchRepository
        );

        counterController = new CounterController(
                queueEngine,
                counterRepository,
                csmRepository,
                tokenRepository,
                tokenService,
                queueBroadcaster
        );
    }

    @Test
    @DisplayName("getDisplayBoard returns called and waiting tokens with branch metadata")
    void getDisplayBoard_returnsPopulatedSnapshot() {
        Branch branch = Branch.builder()
                .id(1L)
                .organizationId(1L)
                .name("Main Campus OPD")
                .build();
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));

        Token calledToken = Token.builder().id(10L).tokenNumber("OPD-010").status("CALLED").build();
        Token waitingToken = Token.builder().id(11L).tokenNumber("OPD-011").status("WAITING").build();

        TokenResponse calledResp = TokenResponse.builder().id(10L).tokenNumber("OPD-010").status("CALLED").build();
        TokenResponse waitingResp = TokenResponse.builder().id(11L).tokenNumber("OPD-011").status("WAITING").build();

        when(tokenRepository.findByBranchIdAndStatusInOrderByCalledAtDesc(eq(1L), any())).thenReturn(List.of(calledToken));
        when(tokenRepository.findByBranchIdAndStatusOrderByCreatedAtAsc(1L, "WAITING")).thenReturn(List.of(waitingToken));
        when(tokenService.mapToTokenResponse(calledToken)).thenReturn(calledResp);
        when(tokenService.mapToTokenResponse(waitingToken)).thenReturn(waitingResp);

        ResponseEntity<ApiResponse<DisplayBoardResponse>> response = queueController.getDisplayBoard(1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        DisplayBoardResponse board = response.getBody().getData();
        assertThat(board.getBranchId()).isEqualTo(1L);
        assertThat(board.getBranchName()).isEqualTo("Main Campus OPD");
        assertThat(board.getCalledTokens()).hasSize(1);
        assertThat(board.getCalledTokens().get(0).getTokenNumber()).isEqualTo("OPD-010");
        assertThat(board.getWaitingTokens()).hasSize(1);
        assertThat(board.getWaitingTokens().get(0).getTokenNumber()).isEqualTo("OPD-011");
    }

    @Test
    @DisplayName("getCounterQueue returns waiting tokens mapped to counter services")
    void getCounterQueue_returnsMappedWaitingTokens() {
        CounterServiceMapping csm1 = CounterServiceMapping.builder().counterId(2L).serviceId(5L).build();
        when(csmRepository.findByCounterId(2L)).thenReturn(List.of(csm1));

        Token t1 = Token.builder().id(20L).serviceId(5L).tokenNumber("DEP-001").status("WAITING").build();
        TokenResponse tr1 = TokenResponse.builder().id(20L).serviceId(5L).tokenNumber("DEP-001").status("WAITING").build();

        when(tokenRepository.findByServiceIdInAndStatusOrderByCreatedAtAsc(List.of(5L), "WAITING")).thenReturn(List.of(t1));
        when(tokenService.mapToTokenResponse(t1)).thenReturn(tr1);

        ResponseEntity<ApiResponse<List<TokenResponse>>> response = counterController.getCounterQueue(2L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        List<TokenResponse> list = response.getBody().getData();
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getTokenNumber()).isEqualTo("DEP-001");
    }

    @Test
    @DisplayName("pauseCounter broadcasts counter status update")
    void pauseCounter_updatesStatusAndBroadcasts() {
        Counter counter = Counter.builder().id(3L).branchId(1L).status("ONLINE").name("Counter 3").build();
        when(counterRepository.findById(3L)).thenReturn(Optional.of(counter));

        ResponseEntity<ApiResponse<String>> response = counterController.pauseCounter(3L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(counter.getStatus()).isEqualTo("PAUSED");
        verify(counterRepository).save(counter);
        verify(queueBroadcaster).broadcastCounterStatus(1L, 3L, "PAUSED");
    }
}
