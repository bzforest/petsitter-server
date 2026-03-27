package com.company.pet_sitter_server.service;

import com.company.pet_sitter_server.dto.upload.FileUploadResponse;
import com.company.pet_sitter_server.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.bucket}")
    private String bucket;

    @Value("${supabase.apiKey}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    /**
     * Uploads a file to Supabase Storage under the given folder.
     *
     * @param file   multipart file from the HTTP request
     * @param folder e.g. "profiles", "pets", "sitters", "payments"
     * @return FileUploadResponse containing the public URL
     */
    public FileUploadResponse upload(MultipartFile file, String folder) {
        validateFile(file);

        String extension = extractExtension(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID() + extension;
        String storagePath = folder + "/" + uniqueFilename;

        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + storagePath;

        try {
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType();

            restClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("x-upsert", "true")   // overwrite if exists
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(bytes)
                    .retrieve()
                    .toBodilessEntity();

            String publicUrl = buildPublicUrl(storagePath);
            return new FileUploadResponse(publicUrl, uniqueFilename, folder, bytes.length, contentType);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to Supabase Storage: " + e.getMessage());
        }
    }

    /**
     * Deletes a file from Supabase Storage.
     *
     * @param storagePath the path inside the bucket, e.g. "profiles/abc.jpg"
     */
    public void delete(String storagePath) {
        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + storagePath;
        try {
            restClient.delete()
                    .uri(deleteUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // log but don't throw — deletion failure should not block the main flow
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size must not exceed 5 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException(
                    "Invalid file type. Allowed: JPEG, PNG, GIF, WEBP");
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        return ".jpg";
    }

    private String buildPublicUrl(String storagePath) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + storagePath;
    }
}
