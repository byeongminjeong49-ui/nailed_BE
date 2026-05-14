package com.nailed.web.settlement.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import java.util.List;

@Getter
public class AdminCompleteRequest {

    @NotEmpty(message = "정산 ID 목록을 입력해주세요.")
    private List<Long> settlementIds;

} //class
