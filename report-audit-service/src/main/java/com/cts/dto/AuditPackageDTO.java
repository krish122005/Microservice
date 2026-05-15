package com.cts.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditPackageDTO {

	private Long packageId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private String contentsJSON;
    private LocalDateTime generatedAt;
    private String packageUri;
    private String checksum;

}
