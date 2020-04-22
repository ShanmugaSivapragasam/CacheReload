package com.shan.CacheReload.contract;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.SortedMap;

@Data
@Slf4j
public class JobRequest {

    String kind;
    Boolean isFunctional;
    Boolean isMiniTest;
    List<String> attributes;
    SortedMap<Integer, String> keys;

}
