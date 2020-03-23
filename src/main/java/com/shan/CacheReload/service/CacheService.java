package com.shan.CacheReload.service;

import com.shan.CacheReload.contract.JobRequest;
import com.shan.CacheReload.contract.JobResponse;
import com.shan.CacheReload.repository.CacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CacheService {


    @Autowired
    CacheRepository cacheRepository;



    public JobResponse reloadData(JobRequest jobRequest) {


        return  cacheRepository.reloadData(jobRequest);
    }
}
