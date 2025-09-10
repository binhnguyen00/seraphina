package me.binhnguyen.seraphina.common;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@NoArgsConstructor
@JsonDeserialize(as = DataRecord.class)
public class DataRecord extends LinkedHashMap<String, Object> {

  public DataRecord(Map<String, Object> data) {
    super(data);
  }

  public static DataRecord spawn() {
    return new DataRecord();
  }

  @JsonAnySetter
  public DataRecord with(String key, Object value) {
    this.put(key, value);
    return this;
  }

  public <T> Optional<T> get(String key, Class<T> type) {
    return getSafe(key, type);
  }

  public List<DataRecord> getAsList(String key) {
    // TODO: implement
    return null;
  }

  public <T> T getOrDefault(String key, Class<T> type, T defaultValue) {
    return getSafe(key, type).orElse(defaultValue);
  }

  private <T> Optional<T> getSafe(String key, Class<T> type) {
    if (!this.containsKey(key)) {
      return Optional.empty();
    }
    Object value = super.get(key);
    if (!type.isInstance(value)) {
      log.warn(
        "Key '{}' exists but type mismatch. Expected: {}, Actual: {}",
        key,
        type.getSimpleName(),
        value.getClass().getSimpleName()
      );
      return Optional.empty();
    }

    return Optional.of(type.cast(value));
  }
}