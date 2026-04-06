package com.company.pet_sitter_server.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class OwnerStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.apiKey}")
    private String supabaseKey;

    /**
     * ฟังก์ชันกลางสำหรับอัปโหลดรูปภาพ
     * @param file ไฟล์รูปภาพจากหน้าเว็บ
     * @param bucketName ชื่อถังที่ต้องการเก็บ (เช่น "owner-profiles")
     * @return URL สาธารณะของรูปภาพ
     */
    public String uploadImage(MultipartFile file, String bucketName) {
        try {
            // 1. ดึงนามสกุลไฟล์ออกมา (เช่น .jpg, .png)
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
    
            // 2. สร้างชื่อไฟล์ใหม่เป็น UUID + นามสกุล (จะไม่ติดภาษาไทยแน่นอน)
            // ผลลัพธ์จะเป็นประมาณ: 211bb50c-dbfa-4431-9d97-6c5cd465fd30.jpg
            String fileName = UUID.randomUUID().toString() + extension;
            
            String endpoint = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;
    
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(supabaseKey);
            headers.set("apikey", supabaseKey);
            headers.setContentType(MediaType.parseMediaType(file.getContentType()));
    
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, String.class);
    
            if (response.getStatusCode().is2xxSuccessful()) {
                return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;
            } else {
                throw new RuntimeException("Upload failed: " + response.getBody());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read file data", e);
        }
    }
}