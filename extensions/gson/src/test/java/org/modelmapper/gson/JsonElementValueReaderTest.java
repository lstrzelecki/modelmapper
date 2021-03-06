package org.modelmapper.gson;

import static org.testng.Assert.assertEquals;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.testng.annotations.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@Test
public class JsonElementValueReaderTest {
  String json = "{\"customer\":{\"streetAddress\":\"123 Main Street\", \"customerCity\": \"SF\"}}";

  public static class Order {
    public Customer customer;
  }

  public static class Customer {
    public Address address;
  }

  public static class Address {
    public String street;
    public String city;
  }

  public void shouldMapFromJsonElement() throws Exception {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration()
        .setFieldMatchingEnabled(true)
        .setMatchingStrategy(MatchingStrategies.LOOSE)
        .addValueReader(new JsonElementValueReader());

    JsonElement element = new JsonParser().parse(json);
    Order o = modelMapper.map(element, Order.class);

    assertEquals(o.customer.address.street, "123 Main Street");
    assertEquals(o.customer.address.city, "SF");
  }
}
