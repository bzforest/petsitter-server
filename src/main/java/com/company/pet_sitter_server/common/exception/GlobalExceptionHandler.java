package com.company.pet_sitter_server.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔥 0. ROLE MISMATCH — user เลือก role ไม่ตรงกับที่มีใน DB
    // ต้องอยู่ก่อน RuntimeException handler เพื่อให้ Spring เลือก handler ที่ specific กว่า
    @ExceptionHandler(RoleMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleRoleMismatch(RoleMismatchException ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("timestamp", LocalDateTime.now());
        res.put("status", 409);
        res.put("code", "ROLE_MISMATCH");
        res.put("accountRole", ex.getAccountRole());
        res.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
    }

    // 🔥 1. BUSINESS / VALIDATION LOGIC ERROR
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {

        Map<String, Object> res = new HashMap<>();
        res.put("timestamp", LocalDateTime.now());
        res.put("status", 400);
        res.put("error", "Bad Request");
        res.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    // 🔥 2. AUTHORIZATION ERROR — SecurityException → 403
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(SecurityException ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("timestamp", LocalDateTime.now());
        res.put("status", 403);
        res.put("error", "Forbidden");
        res.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    // 🔥 2. @VALID DTO ERROR
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage())
        );

        Map<String, Object> res = new HashMap<>();
        res.put("timestamp", LocalDateTime.now());
        res.put("status", 400);
        res.put("error", "Validation Failed");
        res.put("fields", fieldErrors);

        return ResponseEntity.badRequest().body(res);
    }

    // 🔥 3. DATA INTEGRITY ERROR (e.g. Data too long, null constraint)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("timestamp", LocalDateTime.now());
        res.put("status", 400);
        res.put("error", "Data Integrity Error");
        res.put("message", "Data is too long or breaks database constraints. Please check your input.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    // 🔥 4. RUNTIME ERROR (Internal)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleInternalRuntime(RuntimeException ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("timestamp", LocalDateTime.now());
        res.put("status", 500);
        res.put("error", "Internal Server Error");
        res.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    // 🔥 4. SUPABASE AUTH ERROR (email/password ผิด, email ซ้ำใน Supabase ฯลฯ)
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleSupabaseError(HttpClientErrorException ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("timestamp", LocalDateTime.now());
        res.put("status", ex.getStatusCode().value());
        res.put("error", "Authentication Error");
        res.put("message", "Invalid email or password");
        return ResponseEntity.status(ex.getStatusCode()).body(res);
    }

    // 🔥 5. FALLBACK (กันระบบพัง)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {

        Map<String, Object> res = new HashMap<>();
        res.put("timestamp", LocalDateTime.now());
        res.put("status", 500);
        res.put("error", "Internal Server Error");
        res.put("message", "Something went wrong");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }
}