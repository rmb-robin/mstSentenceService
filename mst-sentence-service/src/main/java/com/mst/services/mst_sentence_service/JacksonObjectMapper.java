package com.mst.services.mst_sentence_service;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Provider
public class JacksonObjectMapper implements ContextResolver<ObjectMapper> {

  final ObjectMapper defaultObjectMapper;

  public JacksonObjectMapper() {
    defaultObjectMapper = createDefaultMapper();
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return defaultObjectMapper;
  }

  private static ObjectMapper createDefaultMapper() {
    final ObjectMapper mapper = new ObjectMapper();    
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }
}
