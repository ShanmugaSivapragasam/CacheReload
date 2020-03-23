package com.shan.CacheReload.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Slf4j
public class JobResponse {

    Long totalRecordsLoaded;
    String status;
    String message;
}
