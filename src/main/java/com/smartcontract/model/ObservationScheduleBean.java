package com.smartcontract.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ObservationScheduleBean {
    private String type;
    
    private String value;
    
    private Long nextTime;

    private Boolean executed;
}
