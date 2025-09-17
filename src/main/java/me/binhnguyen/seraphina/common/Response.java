package me.binhnguyen.seraphina.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
  private boolean success;
  private String message;
  private Object data;
}
