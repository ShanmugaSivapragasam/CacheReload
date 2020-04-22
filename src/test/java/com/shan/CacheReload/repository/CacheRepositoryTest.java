package com.shan.CacheReload.repository;


import com.google.cloud.datastore.*;
import com.shan.CacheReload.BaseTest;
import com.shan.CacheReload.contract.JobRequest;
import com.shan.CacheReload.contract.JobResponse;
import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.*;


@RunWith(SpringRunner.class)
@Slf4j
public class CacheRepositoryTest extends BaseTest {


    @MockBean
    Datastore datastoreMock;

    @InjectMocks
    CacheRepository cacheRepository;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

//    @Test
    public void testReloadData_happyPath() throws Exception {
        // prepare data with mocks
        JobRequest jobRequestMock = podamFactory.manufacturePojo(JobRequest.class);
        Long totalRecordsMock = Long.valueOf(new Random().nextInt(5));
        QueryResults<Entity> results = new IteratorEntityImpl<>(totalRecordsMock);
        when(datastoreMock.run(any(Query.class))).thenReturn(results);

        //call the system under test
        JobResponse jobResponse = cacheRepository.reloadDataFromDatastore(jobRequestMock);

        //assert
        assertNotNull("jobResponse ", jobResponse);
        assertEquals(totalRecordsMock +" number of records retrieved are equal "  , totalRecordsMock, jobResponse.getTotalRecordsLoaded());
        verify(datastoreMock, times(1)).run(any(Query.class));

    }


}


