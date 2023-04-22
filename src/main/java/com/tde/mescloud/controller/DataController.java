package com.tde.mescloud.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class DataController {

    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("We are Alive!", HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return new ResponseEntity<>("We are Alive!", HttpStatus.OK);
    }
}
