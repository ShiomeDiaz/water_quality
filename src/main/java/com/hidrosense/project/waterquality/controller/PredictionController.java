package com.hidrosense.project.waterquality.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final RestTemplate restTemplate;
    private static final String PREDICTION_SERVICE_URL = "http://localhost:3035/predict-next-day";

    @Autowired
    public PredictionController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/ica")
    public ResponseEntity<PredictionResponse> predictICA(@RequestBody PredictionRequest request) {
        try {
            // Validar entrada básica
            if (request.getFeatures0To10().size() != 11) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El campo features_0_to_10 debe contener exactamente 11 valores"
                );
            }

            // Configurar headers y enviar solicitud
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<PredictionRequest> entity = new HttpEntity<>(request, headers);

            // Realizar la solicitud al servicio de Python
            ResponseEntity<PredictionResponse> response = restTemplate.exchange(
                    PREDICTION_SERVICE_URL,
                    HttpMethod.POST,
                    entity,
                    PredictionResponse.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al comunicarse con el servicio de predicción: " + e.getMessage(),
                    e
            );
        }
    }

    // Clases DTO
    public static class PredictionRequest {
        private Integer location_ID;
        private LocalDate fecha;
        private List<Double> features_0_to_10;
        private Double pH;
        private Double E_coli;
        private Double Coliformes_totales;
        private Double Turbidez;
        private Double Nitratos;
        private Double Fosfatos;
        private Double DBO5;
        private Double Solidos_suspendidos;

        // Getters y Setters
        public Integer getLocation_ID() { return location_ID; }
        public void setLocation_ID(Integer location_ID) { this.location_ID = location_ID; }

        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }

        public List<Double> getFeatures0To10() { return features_0_to_10; }
        public void setFeatures0To10(List<Double> features_0_to_10) { this.features_0_to_10 = features_0_to_10; }

        public Double getPH() { return pH; }
        public void setPH(Double pH) { this.pH = pH; }

        public Double getE_coli() { return E_coli; }
        public void setE_coli(Double E_coli) { this.E_coli = E_coli; }

        public Double getColiformes_totales() { return Coliformes_totales; }
        public void setColiformes_totales(Double Coliformes_totales) { this.Coliformes_totales = Coliformes_totales; }

        public Double getTurbidez() { return Turbidez; }
        public void setTurbidez(Double Turbidez) { this.Turbidez = Turbidez; }

        public Double getNitratos() { return Nitratos; }
        public void setNitratos(Double Nitratos) { this.Nitratos = Nitratos; }

        public Double getFosfatos() { return Fosfatos; }
        public void setFosfatos(Double Fosfatos) { this.Fosfatos = Fosfatos; }

        public Double getDBO5() { return DBO5; }
        public void setDBO5(Double DBO5) { this.DBO5 = DBO5; }

        public Double getSolidos_suspendidos() { return Solidos_suspendidos; }
        public void setSolidos_suspendidos(Double Solidos_suspendidos) { this.Solidos_suspendidos = Solidos_suspendidos; }
    }

    public static class PredictionResponse {
        private Integer location_id;
        private String fecha_prediccion;
        private Double ica_predicho;

        // Getters y Setters
        public Integer getLocation_id() { return location_id; }
        public void setLocation_id(Integer location_id) { this.location_id = location_id; }

        public String getFecha_prediccion() { return fecha_prediccion; }
        public void setFecha_prediccion(String fecha_prediccion) { this.fecha_prediccion = fecha_prediccion; }

        public Double getIca_predicho() { return ica_predicho; }
        public void setIca_predicho(Double ica_predicho) { this.ica_predicho = ica_predicho; }
    }
}
