package com.hongeee.vacation.domain;

import java.time.Instant;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

  @CreatedDate private Instant createdDate = Instant.now();

  @LastModifiedDate private Instant modifiedDate = Instant.now();

  protected void modifiedDateToNow() {
    modifiedDate = Instant.now();
  }
}
