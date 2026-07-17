package com.demo.warehouse.domain;

import java.time.Instant;

import jakarta.persistence.Embeddable;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class FileInfo {
    private String fileKey;
    private String fileName;
    private String mimeType;
    private Instant uploadedAt = Instant.now();
    private long size;

    @PrePersist
    @PreUpdate
    private void validate() {
        if (fileKey == null || fileName == null || mimeType == null || uploadedAt == null) {
            throw new IllegalStateException(
                "All FileInfo fields must be non-null when FileInfo is present");
        }
    }
}
