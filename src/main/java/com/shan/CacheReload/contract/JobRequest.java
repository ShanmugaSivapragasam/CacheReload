package com.shan.CacheReload.contract;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@Slf4j
public class JobRequest {

    String kind;
    Boolean isFunctional;
    List<String> attributes;
}
