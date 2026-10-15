package com.queueless.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisplayBoardResponse {
    private Long branchId;
    private String branchName;
    private List<TokenResponse> calledTokens;
    private List<TokenResponse> waitingTokens;
}
