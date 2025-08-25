package com.example.backend.Controller;

import com.example.backend.entity.Log;
import com.example.backend.repository.LogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/client")
public class ClientController {


    private final LogRepository logRepository;

    public ClientController(
                            LogRepository logRepository) {

        this.logRepository = logRepository;
    }

}
