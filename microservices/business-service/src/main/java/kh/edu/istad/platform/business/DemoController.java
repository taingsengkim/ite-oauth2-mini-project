package kh.edu.istad.platform.business;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/demo")
public class DemoController {
    @GetMapping
    Map<String, Object> getBusinessById(){
        return Map.of("businessId", UUID.randomUUID().toString());
    }
}
