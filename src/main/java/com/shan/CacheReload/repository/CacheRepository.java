package com.shan.CacheReload.repository;


import com.google.cloud.datastore.*;
import com.shan.CacheReload.contract.JobRequest;
import com.shan.CacheReload.contract.JobResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Iterator;


@Slf4j
@Repository
public class CacheRepository {

    @Autowired
    Datastore datastore;

    public JobResponse reloadData(JobRequest jobRequest) {

        Query<Entity> query =
                Query.newEntityQueryBuilder().setKind(jobRequest.getKind())
                        .build();
        Iterator<Entity> entityIterator = datastore.run(query);

        if(jobRequest.getIsFunctional()){
            return  functionalProcessing(entityIterator);
        }else{
            return  traditionalProcessing(entityIterator);
        }


    }

    //TODO load it into memstore
    private JobResponse functionalProcessing(Iterator<Entity> entityIterator){
        //functional way
        final Long[] count = {0L};
        entityIterator.forEachRemaining(entity -> {
            log.info(entity.toString());//
            count[0]++;

        });
        return new JobResponse(count[0] , "Success", "Processed by functional programming " );
    }

    //TODO load it into memstore
    private JobResponse traditionalProcessing(Iterator<Entity> entityIterator){
        var count = 0L;
        //traditional way
        while(entityIterator.hasNext()){
            Entity entity = entityIterator.next();
            log.info(entity.toString());
            count++;
        }
        return new JobResponse( count, "Success", "Processed by traditional programming " );
    }
}
