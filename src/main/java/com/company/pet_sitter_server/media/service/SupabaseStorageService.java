package com.company.pet_sitter_server.media.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Service
public class SupabaseStorageService {

    private final RestClient restClient;
    private final String supabaseUrl;
    private final String supabaseApiKey;

    public SupabaseStorageService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.apiKey}") String supabaseApiKey
    ) {
        this.supabaseUrl = supabaseUrl;
        this.supabaseApiKey = supabaseApiKey;
        this.restClient = RestClient.builder().baseUrl(supabaseUrl).build();
    }

    public String uploadPublic(MultipartFile file, String bucket) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String originalName = file.getOriginalFilename();
        String safeOriginalName = sanitizeObjectName(originalName);
        if (safeOriginalName.isBlank()) {
            safeOriginalName = "upload";
        }

        String objectName = System.currentTimeMillis() + "-" + safeOriginalName;

        MediaType mediaType;
        String contentTypeHeader = file.getContentType();
        if (contentTypeHeader == null || contentTypeHeader.isBlank()) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        } else {
            try {
                mediaType = MediaType.parseMediaType(contentTypeHeader);
            } catch (Exception e) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read upload file");
        }

        // Supabase Storage public upload endpoint:
        // POST /storage/v1/object/{bucket}/{objectName}
        restClient.post()
                .uri("/storage/v1/object/" + bucket + "/" + objectName)
                .header("apikey", supabaseApiKey)
                .header("Authorization", "Bearer " + supabaseApiKey)
                .contentType(mediaType)
                .body(bytes)
                .retrieve()
                .toBodilessEntity();

        // If bucket is PUBLIC, this is the direct public URL.
        // POSTGRES/Supabase doesn't provide the "object public URL" directly in the response consistently,
        // so we build it deterministically.
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + objectName;
    }

    private static String sanitizeObjectName(String originalName) {
        if (originalName == null) return "";
        // Keep extension, remove any path separators and whitespace.
        String trimmed = originalName.trim();
        trimmed = trimmed.replaceAll("[/\\\\]+", "_");
        trimmed = trimmed.replaceAll("\\s+", "_");
        return trimmed.toLowerCase(Locale.ROOT);
    }
}

