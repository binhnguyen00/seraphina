package me.binhnguyen.seraphina.common;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Response {
  private boolean success;
  private String message;
  private Object data;

  public Response(boolean success, String message, Object data) {
    this.success = success;
    this.message = message;
    this.data = data;
  }

  public Response(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public static Response SUCCESS(String message, Object data) {
    return new Response(true, message, data);
  }

  public static Response SUCCESS(String message) {
    return new Response(true, message);
  }

  public static Response FAIL(String message, Object data) {
    return new Response(false, message, data);
  }

  public static Response FAIL(String message) {
    return new Response(false, message);
  }
}
