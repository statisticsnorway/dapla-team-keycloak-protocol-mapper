package no.ssb.dapla.keycloak.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class HalFlattener {
    public static String flatten(String halJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(halJson);
            flatten(root);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error during JSON processing", e);
        }
    }

    private static void flatten(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            JsonNode embeddedNode = objectNode.get("_embedded");
            if (embeddedNode instanceof ObjectNode) {
                ObjectNode embeddedObjectNode = (ObjectNode) embeddedNode;
                embeddedObjectNode.fields().forEachRemaining(entry -> flatten(entry.getValue()));
                objectNode.remove("_embedded");
                objectNode.setAll(embeddedObjectNode);
            }
            objectNode.remove("_links");
            objectNode.fields().forEachRemaining(entry -> flatten(entry.getValue()));
        }
        else if (node.isArray()) {
            node.elements().forEachRemaining(HalFlattener::flatten);
        }
    }
}