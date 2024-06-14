package no.ssb.dapla.keycloak.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JqTest {

    @Test
    public void queryOne_withOneResult_shouldReturnExpected() {
        assertThat(Jq.queryOne(".someString", SOME_JSON, String.class).orElse(null)).isEqualTo("blah");
        assertThat(Jq.queryOne(".someBoolean", SOME_JSON, Boolean.class).orElse(null)).isEqualTo(true);
        assertThat(Jq.queryOne(".someInteger", SOME_JSON, Integer.class).orElse(null)).isEqualTo(42);
        assertThat(Jq.queryOne(".someLong", SOME_JSON, Long.class).orElse(null)).isEqualTo(123123);
        assertThat(Jq.queryOne(".someFloat", SOME_JSON, Float.class).orElse(null)).isEqualTo(13.37f);
        assertThat(Jq.queryOne(".someDouble", SOME_JSON, Double.class).orElse(null)).isEqualTo(3.14159265358979d);
        assertThat(Jq.queryOne(".someObject", SOME_JSON, SomeObject.class).orElse(null)).isNotNull();
        assertThat(Jq.queryOne(".someList[1]", SOME_JSON, String.class).orElse("")).isEqualTo("dos");
        assertThat(Jq.queryOne(".someList[99999]", SOME_JSON, String.class).orElse("")).isEqualTo("");
        assertThat(Jq.queryOne(".someList", SOME_JSON, new TypeReference<List<String>>() {}).orElse(List.of())).hasSize(3);
        assertThat(Jq.queryOne(".someEmptyList", SOME_JSON, new TypeReference<Set<String>>() {}).orElse(null)).hasSize(0);
        assertThat(Jq.queryOne(".someArray[1].someString", SOME_JSON, String.class).orElse("")).isEqualTo("two");
        assertThat(Jq.queryOne(".someObject.someString", SOME_JSON, String.class).orElse(null)).isEqualTo("blahblah");
        assertThat(Jq.queryOne(".someObject.someNestedObject.someBoolean", SOME_JSON, Boolean.class).orElse(null)).isTrue();
        assertThat(Jq.queryOne("[.someArray[].someString]", SOME_JSON, new TypeReference<List<String>>() {}).orElse(null)).isEqualTo(List.of("one", "two", "three"));
        assertThat(Jq.queryOne("[.someArray[].someNestedObject]", SOME_JSON, new TypeReference<List<SomeObject>>() {}).orElse(null)).hasSize(3);
    }

    @Test
    public void queryOne_withNoResult_shouldReturnEmpty() {
        assertThat(Jq.queryOne(".none", SOME_JSON, String.class).orElse(null)).isEqualTo(null);
    }

    @Test
    public void queryOne_withMultipleResults_shouldThrowException() {
        Jq.JqException e = assertThrows(Jq.JqException.class, () -> {
            Jq.queryOne(".someString, .someBoolean", SOME_JSON, String.class);
        });
        assertThat(e.getMessage()).startsWith("Expected JQ expression to match a single value, but multiple matches was found");
    }

    private final String SOME_JSON = """
    {
      "someString": "blah",
      "someBoolean": true,
      "someInteger": 42,
      "someLong": 123123,
      "someFloat": 13.37,
      "someDouble": 3.14159265358979,
      "someDate": "2020-06-18T11:58:00.408421Z",
      "someList": ["uno", "dos", "tres"],
      "someEmptyList": [],
      "someNull": null,
      "someObject": {
        "someString": "blahblah",
        "someInteger": 42,
        "someBoolean": true,
        "someNestedObject": {
          "someString": "blahblah",
          "someInteger": 42,
          "someBoolean": true
        }
      },
      "someArray": [
        {
          "someString": "one",
          "someInteger": 1,
          "someBoolean": true,
          "someNestedObject": {
            "someString": "object-one",
            "someBoolean": true
          }
        },
        {
          "someString": "two",
          "someInteger": 2,
          "someBoolean": false,
          "someNestedObject": {
            "someString": "object-two",
            "someBoolean": true
          }
        },
        {
          "someString": "three",
          "someInteger": 3,
          "someBoolean": true,
          "someNestedObject": {
            "someString": "object-three",
            "someBoolean": false
          }
        }
      ]
    }    
    """;

    private static class SomeObject {
        public String someString;
        public Boolean someBoolean;
        public Integer someInteger;
        public Long someLong;
        public Float someFloat;
        public Double someDouble;
    }

}