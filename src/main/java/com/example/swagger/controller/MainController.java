package com.example.swagger.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
public class MainController {
    Random random = new Random();

    @GetMapping("/double")
    @Tag(name = "${tags.get.double}")
    @Operation(summary = "${summary.get.double}",
            description = "${description.get.double}")
    public Double getDouble(@Parameter(description = "${params.lower.bound}") @RequestParam double lowerBound,
                            @Parameter(description = "${params:high.bound}") @RequestParam double highBound) {

       return random.doubles(lowerBound, highBound).findFirst().orElse(10.10);
    }

    @PostMapping("/int")
    @Tag(name = "${tags.post.int}")
    @Operation(summary = "${summary.post.int}",
            description = "${description.post.int}")
    public Integer getInt(@Parameter(description = "${params.lower.bound}") @RequestParam int lowerBound,
                          @Parameter(description = "${params.high.bound}") @RequestParam int highBound) {

        return random.ints(lowerBound, highBound).findFirst().orElse(1);
    }

    @PutMapping("/long")
    @Tag(name = "${tags.put.long}")
    @Operation(summary = "${summary.put.long}",
            description = "${description.put.long}")
    public Long getLong(@Parameter(description = "${params.lower.bound}") @RequestParam long lowerBound,
                        @Parameter(description = "${params.high.bound}") @RequestParam long highBound) {

        return random.longs(lowerBound, highBound).findFirst().orElse(1000000);
    }
}
