package com.alcegory.mescloud.api.rest;

import com.alcegory.mescloud.model.dto.RequestSampleDto;
import com.alcegory.mescloud.model.dto.SampleDto;
import com.alcegory.mescloud.service.SampleService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sample")
@AllArgsConstructor
public class SampleController {

    private final SampleService sampleService;

    @PostMapping
    public ResponseEntity<SampleDto> create(@RequestBody RequestSampleDto requestSampleDto) {
        SampleDto sampleDto = sampleService.create(requestSampleDto);
        if (sampleDto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(sampleDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<SampleDto>> findAll() {
        List<SampleDto> sampleDtos = sampleService.getAll();
        return new ResponseEntity<>(sampleDtos, HttpStatus.OK);
    }
}