package no.ssb.dapla.keycloak.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Converter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <T> T convert(Object fromValue, Class<T> toValueType) {
        return OBJECT_MAPPER.convertValue(fromValue, toValueType);
    }

    public static <T> T convert(Object fromValue, TypeReference<T> type) {
        return OBJECT_MAPPER.convertValue(fromValue, type);
    }

}
