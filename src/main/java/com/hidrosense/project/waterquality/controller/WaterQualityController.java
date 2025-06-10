package com.hidrosense.project.waterquality.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class WaterQualityController {

    private final RestTemplate restTemplate;

    @Autowired
    public WaterQualityController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Endpoint para obtener el reporte de ICA con múltiples opciones de filtro:
     * - fecha exacta y Location_ID
     * - rango de fechas
     * - rango de fechas y Location_ID
     *
     * Ejemplos de uso:
     * GET /api/ica-report?fecha=2025-06-10&location_id=2336240
     * GET /api/ica-report?fecha_inicio=2025-06-01&fecha_fin=2025-06-10
     * GET /api/ica-report?fecha_inicio=2025-06-01&fecha_fin=2025-06-10&location_id=2336240
     */
    @GetMapping("/ica-report")
    public ResponseEntity<?> getIcaReport(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String fecha_inicio,
            @RequestParam(required = false) String fecha_fin,
            @RequestParam(required = false) Integer location_id
    ) {
        // Construir URL base
        String baseUrl = "http://localhost:3033/registros";

        // Construir parámetros dinámicamente
        Map<String, String> params = new HashMap<>();

        if (fecha != null && !fecha.isEmpty()) {
            params.put("fecha", fecha);
        }
        if (fecha_inicio != null && !fecha_inicio.isEmpty()) {
            params.put("fecha_inicio", fecha_inicio);
        }
        if (fecha_fin != null && !fecha_fin.isEmpty()) {
            params.put("fecha_fin", fecha_fin);
        }
        if (location_id != null) {
            params.put("location_id", location_id.toString());
        }

        // Construir query string
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        if (!params.isEmpty()) {
            urlBuilder.append("?");
            params.forEach((key, value) -> urlBuilder.append(key).append("=").append(value).append("&"));
            urlBuilder.deleteCharAt(urlBuilder.length() - 1); // Eliminar último &
        }

        String url = urlBuilder.toString();

        // Realizar petición GET
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
