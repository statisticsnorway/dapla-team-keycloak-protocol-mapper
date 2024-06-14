package no.ssb.dapla.keycloak.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.Map;

public class Json {

    private static final Moshi MOSHI = new Moshi.Builder().build();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    /**
     * Get Moshi adapter for class
     */
    public static JsonAdapter<Map<String,Object>> genericMapAdapter() {
        return MOSHI.adapter(new TypeReference<Map<String, Object>>() {}.getType());
    }

    public static <T> JsonAdapter<T> adapter(Class<T> type) {
        return MOSHI.adapter(type).lenient(); // FIXME remove lenient?
    }

    /**
     * Convert JSON to Object
     */
    public static <T> T toObject(Class<T> type, String json) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        }
        catch (IOException e) {
            throw new JsonException("Error mapping JSON to " + type.getSimpleName() + " object", e);
        }
    }

    /**
     * Convert JSON to Object
     *
     * Use with generics, like new TypeReference<HashMap<MyPair, String>>() {}
     */
    public static <T> T toObject(TypeReference<T> type, String json) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        }
        catch (IOException e) {
            throw new JsonException("Error mapping JSON to " + type.getType() + " object", e);
        }
    }

    /**
     * Convert JSON to Object
     */
    public static <T> T toObject(Class<T> type, JsonNode json) {
        try {
            return OBJECT_MAPPER.treeToValue(json, type);
        }
        catch (IOException e) {
            throw new JsonException("Error mapping JSON to " + type.getSimpleName() + " object", e);
        }
    }

    /**
     * Convert JSON to Object
     *
     * Use with generics, like new TypeReference<HashMap<MyPair, String>>() {}
     */
    public static <T> T toObject(TypeReference<T> type, JsonNode json) {
        try {
            return OBJECT_MAPPER.treeToValue(json, type);
        }
        catch (IOException e) {
            throw new JsonException("Error mapping JSON to " + type.getType() + " object", e);
        }
    }

    /**
     * Convert JSON to JsonNode
     */
    public static JsonNode toJsonNode(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        }
        catch (IOException e) {
            throw new JsonException("Error mapping JSON to JsonNode object", e);
        }
    }

    /**
     * Convert Map to JsonNode
     */
    public static JsonNode toJsonNode(Map<String, Object> map) {
        return OBJECT_MAPPER.valueToTree(map);
    }

    /**
     * Convert JSON to String->Object map
     */
    public static Map<String, Object> toGenericMap(String json) {
        return toObject(new TypeReference<>() {}, json);
    }

    /**
     * Convert Object to JSON
     */
    public static String from(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonException("Error mapping " +  object.getClass().getSimpleName() + " object to JSON", e);
        }
    }

    /**
     * Convert Object to pretty (indented) JSON
     */
    public static String prettyFrom(Object object) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonException("Error mapping " +  object.getClass().getSimpleName() + " object to JSON", e);
        }
    }

    /**
     * Pretty print (indent) JSON string
     */
    public static String prettyFrom(String string) {
        return prettyFrom(toObject(Object.class, string));
    }

    public static class JsonException extends RuntimeException {
        public JsonException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
