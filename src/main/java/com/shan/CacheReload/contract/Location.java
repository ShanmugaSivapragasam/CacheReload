package com.shan.CacheReload.contract;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Time;

@Data
@AllArgsConstructor
@Slf4j
public class Location {

    private String locnNbr;
    private String locationName;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String locationType;
    private Time openTime;
    private Time closeTime;
}
