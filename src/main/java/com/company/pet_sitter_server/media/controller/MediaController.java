package com.company.pet_sitter_server.media.controller;

import com.company.pet_sitter_server.media.service.SupabaseStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping({"/api/media", "/media"})
public class MediaController {

    private final SupabaseStorageService storageService;

    public MediaController(SupabaseStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "profile-images") String bucket
    ) {
        String url = storageService.uploadPublic(file, bucket);
        return ResponseEntity.ok(Map.of("url", url));
    }
}

