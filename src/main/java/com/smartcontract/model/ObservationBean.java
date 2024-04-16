package com.smartcontract.model;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ObservationBean {
    private String name;
    
    private String shouldRun;
    
    private ObservationScheduleBean observationSchedule;
    
    private ExternalParamBean externalParams;

    private InternalParamBean internalParams;

    private ExtraParamBean extraParams;

    private List<ReturnParamBean> returnParams;
    
    private ObservationLogicBean observationLogic;
}
