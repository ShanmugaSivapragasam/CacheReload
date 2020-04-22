package com.shan.CacheReload.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import java.io.IOException;


public class SerializationHelper {

    public static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JaxbAnnotationModule());
    }

    public  static byte[] serializeObject(Object obejct) throws JsonProcessingException {
        return  objectMapper.writeValueAsBytes(obejct);
    }

    public static Object deSerilalize(byte[] objectArray, Class objectClass) throws IOException {
        return objectMapper.readValue(objectArray, objectClass);
    }


}
