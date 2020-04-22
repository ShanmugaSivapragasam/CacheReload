package com.shan.CacheReload;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootApplication
@Slf4j
public class CacheReloadApplication {
    @Value("${GOOGLE_APPLICATION_CREDENTIALS:none}")
    private String jsonPath;

    @Value("${redis_host:localhost}")
    private String redisHost;

    @Value("${redis_port:6379}")
    private Integer redisPort;

    public static void main(String[] args) {
        SpringApplication.run(CacheReloadApplication.class, args);
    }

    @Bean
    public Datastore datatStore() {
        log.info("json path " + jsonPath);

        GoogleCredentials credentials = null;
        Datastore datastore = null;
        try {
            if (jsonPath.equals("none") == false) {

                credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
                        .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
                datastore = DatastoreOptions.newBuilder().setCredentials(credentials).build().getService();
            }else {
                datastore = DatastoreOptions.getDefaultInstance().getService();
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return datastore;

    }


    @Bean
    public Firestore firestore() {

        GoogleCredentials credentials = null;
        log.info("Firestore - json path " + jsonPath);


        try {

            if (jsonPath.equals("none")) {
                credentials = GoogleCredentials.getApplicationDefault();
                log.info("default credential " + credentials);
                FirestoreOptions firestoreOptions =  FirestoreOptions.newBuilder()
                        .setCredentials(credentials)
//                        .setProjectId(projectId)
                        .build();

                Firestore firestore = firestoreOptions.getService();

//                FirestoreOptions.newBuilder()
//                        .setProjectId("shan-demos2")
//                        .build();
//                Firestore firestore = FirestoreOptions.getDefaultInstance().getService();
//                log.info("fire store initialized "+ firestore);
                return firestore;


            } else {
                credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
                        .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
                log.info(" elevated credentials " + credentials);
                FirestoreOptions firestoreOptions =
                        FirestoreOptions.newBuilder()
                                .setCredentials(credentials)
                                .build();
                Firestore firestore =  firestoreOptions.getService();
                return  firestore;
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

    @Bean
    public JedisPool createJedisPool() throws IOException {

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // Default : 8, consider how many concurrent connections into Redis you will need under load
        poolConfig.setMaxTotal(500); //holiday peak had 684 concurrent request per second
        poolConfig.setMaxIdle(200); //holiday peak had 684 concurrent request per second

        return new JedisPool(poolConfig, redisHost, redisPort);
    }

}
