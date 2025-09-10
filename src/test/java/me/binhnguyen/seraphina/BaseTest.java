package me.binhnguyen.seraphina;

import me.binhnguyen.seraphina.common.DataRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class BaseTest {

  @Test
  void testNewRecord_1() {
    DataRecord record = new DataRecord(Map.of(
      "name", "John",
      "age", 30
    ));
    Assertions.assertEquals("John", record.get("name"));
    Assertions.assertEquals(30, record.get("age"));
  }

  @Test
  void testNewRecord_2() {
    DataRecord record = new DataRecord()
      .with("name", "John")
      .with("age", 30);
    Assertions.assertEquals("John", record.get("name"));
    Assertions.assertEquals(30, record.get("age"));
  }

  @Test
  void testNewRecord_3() {
    DataRecord record = DataRecord.spawn()
      .with("name", "John")
      .with("age", 30);
    Assertions.assertEquals("John", record.get("name"));
    Assertions.assertEquals(30, record.get("age"));
  }
}
