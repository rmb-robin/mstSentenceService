package com.mst.services.mst_sentence_service;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

@ApplicationPath("/")
public class MyApplication extends ResourceConfig {
 
    public MyApplication() {
        packages("com.mst.services.mst_sentence_service");
    	ObjectMapper objectMapper = new ObjectMapper();
    	objectMapper.findAndRegisterModules();
    	objectMapper.registerModule(new JavaTimeModule());
    	// Register my custom provider - not needed if it's in my.package.
       // register(JacksonFeature.class);
    	register(JacksonObjectMapper.class);  
    	register(JacksonJaxbJsonProvider.class);
    }
}
