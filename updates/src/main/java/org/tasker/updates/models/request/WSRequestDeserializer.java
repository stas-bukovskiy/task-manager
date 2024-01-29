package org.tasker.updates.models.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WSRequestDeserializer extends StdDeserializer<WSRequest> {

    public WSRequestDeserializer() {
        super(WSRequest.class);
    }

    @Override
    public WSRequest deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (!node.has("correlation_id") || !node.has("type") || !node.has("data")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields");
        }

        String correlationId = node.get("correlation_id").asText();
        String type = node.get("type").asText();
        byte[] data = node.get("data").toString().getBytes(StandardCharsets.UTF_8);
        return new WSRequest(correlationId, type, data);
    }
}
