package me.binhnguyen.seraphina.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass // parent class, can't be entity
@EntityListeners(AuditingEntityListener.class)
abstract public class BaseEntity implements Persistable<Long>, Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;

  @CreatedDate
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DatePattern.TIMESTAMP)
  @Column(name = "created_time")
  protected LocalDateTime createdTime;

  @LastModifiedDate
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DatePattern.TIMESTAMP)
  @Column(name = "modified_time")
  protected LocalDateTime modifiedTime;

  @Override
  public boolean isNew() {
    return Objects.isNull(this.id);
  }

  public boolean exists() {
    return !this.isNew();
  }
}