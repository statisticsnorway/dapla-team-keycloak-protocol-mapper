package no.ssb.dapla.keycloak.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HalFlattenerTest {

    String halJson = """
            {
              "_links": {
                "self": {
                  "href": "/order"
                }
              },
              "_embedded": {
                "orders": [
                  {
                    "_links": {
                      "self": {
                        "href": "/order/123"
                      },
                      "basket": {
                        "href": "/basket/98712"
                      },
                      "customer": {
                        "href": "/customer/7809"
                      }
                    },
                    "total": 30.00,
                    "currency": "USD",
                    "status": "shipped",
                    "_embedded": {
                      "items": [
                        {
                          "_links": {
                            "self": {
                              "href": "/item/1"
                            }
                          },
                          "name": "Item 1",
                          "price": 10.00,
                          "_embedded": {
                            "details": {
                              "_links": {
                                "self": {
                                  "href": "/item/1/details"
                                }
                              },
                              "description": "Detailed description",
                              "manufacturer": "Manufacturer name"
                            }
                          }
                        }
                      ]
                    }
                  }
                ]
              }
            }
            """;

    @Test
    void testFlattenWithDeeplyNestedHalDocument() throws Exception {
        String flatJson = HalFlattener.flatten(halJson);
        String expectedJson = """
                {
                  "orders": [
                    {
                      "total": 30.00,
                      "currency": "USD",
                      "status": "shipped",
                      "items": [
                        {
                          "name": "Item 1",
                          "price": 10.00,
                          "details": {
                            "description": "Detailed description",
                            "manufacturer": "Manufacturer name"
                          }
                        }
                      ]
                    }
                  ]
                }
                """;

        // assert the returned results
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actual = mapper.readTree(flatJson);
        JsonNode expected = mapper.readTree(expectedJson);

        assertEquals(expected, actual, "Flattened JSON did not match the expected result");

    }
}