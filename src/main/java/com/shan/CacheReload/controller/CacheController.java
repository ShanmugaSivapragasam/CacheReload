package com.shan.CacheReload.controller;

import com.shan.CacheReload.contract.JobRequest;
import com.shan.CacheReload.contract.JobResponse;
import com.shan.CacheReload.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class CacheController {


    @Autowired
    CacheService cacheService;

    @PostMapping("/memstore/stores")
    public ResponseEntity<JobResponse> reloadCache(@RequestBody JobRequest jobRequest) throws Exception{
//        JobResponse response = cacheService.reloadDataFromDatastore(jobRequest);
        JobResponse response = cacheService.reloadDataFromFirestore(jobRequest);
        return  new ResponseEntity<>(response,HttpStatus.ACCEPTED);
    }

    @Deprecated
    @PostMapping("/datastore/stores")
    public ResponseEntity<String> writeToDataStore(@RequestBody JobRequest jobRequest) throws Exception{

        String response = cacheService.loadDataToDataStore(jobRequest);
        return  new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/firestore/stores")
    public ResponseEntity<String> writeToFireStore(@RequestBody JobRequest jobRequest) throws Exception{

        String response = cacheService.loadDataToFireStore(jobRequest);
        return  new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }


    @GetMapping("/cache/stores/{id}")
    public  ResponseEntity<Map<String, String>> getLocationDetails(@PathVariable String id) throws Exception{

        Map<String, String> response = cacheService.getLocationDetails(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/firestore/stores/{id}")
    public ResponseEntity<Map<String, Object>> getLocationDetailsFromFirestore(@PathVariable String id) throws  Exception{
        Map<String, Object> response = cacheService.getLocationDetailsFromFirestore(id);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }
}

