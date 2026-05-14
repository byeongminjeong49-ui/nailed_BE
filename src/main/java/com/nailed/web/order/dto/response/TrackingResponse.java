package com.nailed.web.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingResponse {

    private String               trackingNumber;
    private String               carrier;
    private String               status;
    private List<TrackingDetail> details;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingDetail {
        private String status;
        private String location;
        private String time;
    }

} //class
