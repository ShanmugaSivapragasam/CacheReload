package com.shan.CacheReload.controller;

import com.shan.CacheReload.contract.JobRequest;
import com.shan.CacheReload.contract.JobResponse;
import com.shan.CacheReload.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheController {


    @Autowired
    CacheService cacheService;

    @PostMapping("/cache")
    public ResponseEntity<JobResponse> reloadCache(@RequestBody JobRequest jobRequest){
        JobResponse response = cacheService.reloadData(jobRequest);
        return  new ResponseEntity<>(response,HttpStatus.ACCEPTED);
    }
}

