package com.shan.CacheReload.service;


import com.shan.CacheReload.contract.JobRequest;
import com.shan.CacheReload.contract.JobResponse;
import com.shan.CacheReload.repository.CacheRepository;
import com.shan.CacheReload.util.SerializationHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.shan.CacheReload.util.Constants.*;


@Service
@Slf4j
public class CacheService {


    @Autowired
    CacheRepository cacheRepository;


    @Deprecated
    public JobResponse reloadDataFromDatastore(JobRequest jobRequest) throws Exception {
        return cacheRepository.reloadDataFromDatastore(jobRequest);
    }

    public JobResponse reloadDataFromFirestore(JobRequest jobRequest) throws Exception{
        return  cacheRepository.reloadDataFromFirestore(jobRequest);
    }

    @Deprecated
    public String loadDataToDataStore(JobRequest jobRequest) throws IOException {

        List<Map<String, Object>> records = extractDataForLoading(jobRequest);

        log.info("loading total records of " + records.size());
        List result = cacheRepository.loadDataTodataStore(jobRequest.getKind(), records);

        return SerializationHelper.objectMapper.writeValueAsString(result);
    }

    public String loadDataToFireStore(JobRequest jobRequest) throws  Exception{

        List<Map<String, Object>> records = extractDataForLoading(jobRequest);

        log.info("loading total records of " + records.size());
        int recordsAdded = cacheRepository.loadDataToFireStore(jobRequest.getKind(), records);
        Map<String, Integer> response = new HashMap<>();
        response.put("recordsAdded", recordsAdded );

        return SerializationHelper.objectMapper.writeValueAsString(response);
    }

    private List<Map<String, Object>> extractDataForLoading(JobRequest jobRequest) throws IOException {
        //TODO make this as dynamic storage volume
        Reader reader = Files.newBufferedReader(Paths.get("/Users/M_783062/Shan/git/cachereload/locations.csv"));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                .withFirstRecordAsHeader());

        //"locnNbr","","","","","MAXPICKSQUANTITY",""
        List<Map<String, Object>> records = new ArrayList<>();

        int count = 0;
        for (Iterator<CSVRecord> iterator = csvParser.iterator(); iterator.hasNext(); ) {
            CSVRecord csvRecord = iterator.next();
            Map<String, Object> record = new HashMap<>();
            // Access
            List<String> headers = csvParser.getHeaderNames();

            Iterator<String> headersItr = headers.iterator();

            while (headersItr.hasNext()) {
                String colName = headersItr.next();
                record.put(colName, csvRecord.get(colName));

            }
            Map<Integer, String> jobRequestFilterKeys = jobRequest.getKeys();

            StringBuilder fitlerKeySB = new StringBuilder();
            StringBuilder keyValue = new StringBuilder();

            for (Map.Entry<Integer, String> entry : jobRequestFilterKeys.entrySet()) {
                fitlerKeySB.append(csvRecord.get(entry.getValue()))
                        .append(SEPARATOR);
            }
            String filterKey = fitlerKeySB.deleteCharAt(fitlerKeySB.length() - 1).toString();
            record.put(KEY, filterKey);
            records.add(record);
//            log.info("the record " + record);
            count++;
            if( jobRequest.getIsMiniTest() && count >10){
                log.info( "Mini test restricting the complete loading ");
                break;
            }

        }
        return records;
    }

    public Map<String, String> getLocationDetails(String key) throws Exception {

        Map<String, String> location = cacheRepository.readFromRedis(key);
        log.info(" read from redis for key " + key + "location details " + location );
        return  location;
//        return SerializationHelper.objectMapper.writeValueAsString(locationCapacityList);
    }


    public Map<String, Object> getLocationDetailsFromFirestore(String key) throws Exception {
        Map<String, Object> location = cacheRepository.readLocationFromFirestore(key);
        log.info(" read from Firestore for key " + key + "location details " + location );
        return  location;
    }

}
