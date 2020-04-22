package com.shan.CacheReload.repository;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;

import com.google.cloud.datastore.Query;
import com.google.cloud.firestore.*;
import com.shan.CacheReload.contract.JobRequest;
import com.shan.CacheReload.contract.JobResponse;
import com.shan.CacheReload.contract.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.shan.CacheReload.util.Constants.*;


@Slf4j
@Repository
public class CacheRepository {

    @Autowired
    Datastore datastore;

    @Autowired
    JedisPool jedisPool;

    @Autowired
    Firestore firestore;


    @Deprecated
    public JobResponse reloadDataFromDatastore(JobRequest jobRequest) throws Exception {

        Query<Entity> query =
                Query.newEntityQueryBuilder().setKind(jobRequest.getKind())
                        .build();
        Iterator<Entity> entityIterator = datastore.run(query);

        if (jobRequest.getIsFunctional()) {
            return functionalProcessing(entityIterator, jobRequest);
        } else {
            return traditionalProcessing(entityIterator, jobRequest);
        }


    }


    private JobResponse functionalProcessing(Iterator<Entity> entityIterator, JobRequest jobRequest) {
        //functional way
        final Long[] count = {0L};
        entityIterator.forEachRemaining(entity -> {

            Map<String, Value<?>> properties = entity.getProperties();
            StringBuilder fitlerKeySB = new StringBuilder();
            Map<Integer, String> jobRequestFilterKeys = jobRequest.getKeys();
            List<String> attributesReq = jobRequest.getAttributes();
            Map<String, String> attributesToStore = new HashMap<>();

            jobRequestFilterKeys.entrySet()
                    .forEach(entry ->
                            fitlerKeySB.append(properties.get(entry.getValue()).get())
                                    .append(SEPARATOR));
            String filterKey = fitlerKeySB.deleteCharAt(fitlerKeySB.length() - 1).toString();

            attributesReq.forEach(attribute -> {
                attributesToStore.put(attribute, String.valueOf(properties.get(attribute).get()));

            });


            Jedis jedis = jedisPool.getResource();
            jedis.hmset(filterKey, attributesToStore);
            jedis.close();
            count[0]++;

        });
        return new JobResponse(count[0], "Success", "Processed by functional programming ");
    }

    private JobResponse traditionalProcessing(Iterator<Entity> entityIterator, JobRequest jobRequest) throws JsonProcessingException {
        var count = 0L;
        //traditional way
        while (entityIterator.hasNext()) {

            Entity entity = entityIterator.next();
            Map<String, Value<?>> properties = entity.getProperties();
            StringBuilder fitlerKeySB = new StringBuilder();
            Map<Integer, String> jobRequestFilterKeys = jobRequest.getKeys();
            List<String> attributesReq = jobRequest.getAttributes();
            Map<String, String> attributesToStore = new HashMap<>();

            for (Map.Entry<Integer, String> entry : jobRequestFilterKeys.entrySet()) {
                fitlerKeySB.append(properties.get(entry.getValue()).get())
                        .append(SEPARATOR);
            }
            String filterKey = fitlerKeySB.deleteCharAt(fitlerKeySB.length() - 1).toString();

            for (String attribute : attributesReq) {
                attributesToStore.put(attribute, String.valueOf(properties.get(attribute).get()));
//                attributesToStore.put(SerializationHelper.serializeObject(attribute), SerializationHelper.serializeObject(properties.get(attribute).get()));
            }
            log.info(" Inserting " + count + "th record " + filterKey);
            Jedis jedis = jedisPool.getResource();
//            jedis.hmset(SerializationHelper.serializeObject(filterKey), attributesToStore);
            jedis.hmset(filterKey, attributesToStore);
            jedis.close();
            count++;
        }
        return new JobResponse(count, "Success", "Processed by traditional programming ");
    }

    @Deprecated
    public List<Entity> loadDataTodataStore(String kindName, List<Map<String, Object>> records) {

        List<Entity> entities = new ArrayList<>();

        for (Map<String, Object> record : records) {

            Key taskKey = datastore.newKeyFactory()
                    .setKind(kindName)
                    .newKey((String) record.get(KEY));
            Entity.Builder entityBuilder = Entity.newBuilder(taskKey);

            for (Map.Entry<String, Object> entry : record.entrySet()) {
                entityBuilder.set(entry.getKey(), String.valueOf(entry.getValue()));
            }
            entityBuilder.set(LAST_UPD_TS, Timestamp.now());
            entityBuilder.set(LAST_UPD_USER, "Default");

            Entity recordForUpsert = entityBuilder.build();
            Entity result = datastore.put(recordForUpsert);
            entities.add(result);

        }

        return entities;
    }

    public int loadDataToFireStore(String kindName, List<Map<String, Object>> records) throws Exception {


        List<ApiFuture<WriteResult>> results = new ArrayList<>();
        AtomicInteger beforeUpdate = new AtomicInteger(0);
        long start = System.nanoTime();

//        firestore.collection(kindName).listDocuments().forEach( documentReference -> {
//            beforeUpdate.getAndIncrement();
//        });

        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        log.info("time elapsed for  before count = " + +((double) timeElapsed / 1_000_000_000));
        start = System.nanoTime();
        for (Map<String, Object> record : records) {
            DocumentReference docRef = firestore.collection(kindName).document((String) record.get(KEY));
            record.remove(KEY);
            record.put(LAST_UPD_TS, FieldValue.serverTimestamp());
            //asynchronously write data
            results.add(docRef.set(record));
        }

        finish = System.nanoTime();
        timeElapsed = finish - start;
        log.info("time elapsed for adding data  = " + +((double) timeElapsed / 1_000_000_000));

        start = System.nanoTime();

        for (ApiFuture<WriteResult> result : results) {
            WriteResult writeResult = result.get();
        }

        finish = System.nanoTime();
        timeElapsed = finish - start;
        log.info("time elapsed for getting result    = " + ((double) timeElapsed / 1_000_000_000));

        AtomicInteger afterUpdate = new AtomicInteger(0);
        start = System.nanoTime();
//        firestore.collection(kindName).listDocuments().forEach( documentReference -> afterUpdate.getAndIncrement());

        finish = System.nanoTime();
        timeElapsed = finish - start;
        log.info("time elapsed for getting final count    = " + +((double) timeElapsed / 1_000_000_000));

        return afterUpdate.get() - beforeUpdate.get();

    }

    public List<Map<String, String>> readFromRedis(List<Location> locations) {

        StringBuilder keySB = new StringBuilder();
        List<Map<String, String>> mapList = new ArrayList<>();
        for (Location location : locations) {
            keySB.append(location.getLocnNbr()).append(SEPARATOR).append(location.getLocationName()).append(SEPARATOR);
            Jedis jedis = jedisPool.getResource();

            for (LOCATION_TYPES location_type : LOCATION_TYPES.values()) {
                keySB.append(location_type);
                Map<String, String> redisRecord = jedis.hgetAll(keySB.toString());
                if (redisRecord.size() > 0) {
                    mapList.add(redisRecord);
                }
                jedis.close();
            }
        }

        return mapList;

    }

    public JobResponse reloadDataFromFirestore(JobRequest jobRequest) throws Exception {
        Iterable<DocumentReference> documentReferences = firestore.collection(jobRequest.getKind()).listDocuments();

        AtomicInteger count = new AtomicInteger();
        documentReferences.forEach(documentReference -> {
            ApiFuture<DocumentSnapshot> documentSnapshotApiFuture = documentReference.get();
            DocumentSnapshot documentSnapshot = null;
            try {
                documentSnapshot = documentSnapshotApiFuture.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            log.info(" document retrieved " + documentSnapshot.getId());
            StringBuilder redisKey = new StringBuilder();


            Map<String, String> redisAttributes = new HashMap<>();
            if (documentSnapshot.exists()) {
                Map<String, Object> data = documentSnapshot.getData();
                //may be simply can use document id
                jobRequest.getKeys().entrySet().forEach(keyEntry -> redisKey.append(data.get(keyEntry.getValue())));
                jobRequest.getAttributes().forEach(attribute -> redisAttributes.put(attribute, String.valueOf(data.get(attribute))));


                Jedis jedis = jedisPool.getResource();
                jedis.hmset(redisKey.toString(), redisAttributes);
                count.getAndIncrement();
                jedis.close();
                log.info("jedis closed for " + documentSnapshot.getId());
            }

        });
        return new JobResponse(count.longValue(), "Success", "Processed by functional (default for now) programming ");

    }

    public Map<String, String> readFromRedis(String redisKey) {
        Jedis jedis = jedisPool.getResource();
        Map<String, String> response = jedis.hgetAll(redisKey );

        jedis.close();
        return response;

    }
}
