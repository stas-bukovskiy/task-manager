package org.tasker.updates.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.tasker.updates.models.request.WSRequest;
import org.tasker.updates.models.request.WSRequestDeserializer;
import org.tasker.updates.models.response.ErrorMessages;
import org.tasker.updates.service.WSRequestDeserializeService;

import java.io.IOException;

@Service
public class WSRequestDeserializeServiceImpl implements WSRequestDeserializeService {

    private final ObjectMapper objectMapper;

    public WSRequestDeserializeServiceImpl() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(WSRequest.class, new WSRequestDeserializer());
        objectMapper.registerModule(module);
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public WSRequest deserialize(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, WSRequest.class);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.BAD_WS_REQUEST);
        }
    }
}
