package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestCallController {

    @GetMapping("/test")
    public ResponseEntity<String> getOk() {
        return ResponseEntity.ok("OK");
    }
}