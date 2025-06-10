package com.hidrosense.project.waterquality.services;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class WaterQualityService {
    private final RestTemplate restTemplate = new RestTemplate();

    public Object getIcaReport(String fecha, int locationId) {
        String url = "http://localhost:8000/registros?fecha=" + fecha + "&Location_ID=" + locationId;
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
        return response.getBody();
    }
}
