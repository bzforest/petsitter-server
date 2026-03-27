package com.company.pet_sitter_server.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadResponse {
    private String url;
    private String filename;
    private String folder;
    private long sizeBytes;
    private String contentType;
}
