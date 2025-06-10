package com.hidrosense.project.waterquality.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predict")
@CrossOrigin // permite llamadas desde React u otro frontend
public class PredictionController {

    @GetMapping
    public String getPrediction() {
        return "Water quality prediction: SAFE ✅";
    }

    @PostMapping
    public String postPrediction(@RequestBody PredictionInput input) {
        // Simulación de lógica con los datos recibidos
        return "Received data: pH = " + input.getPh() +
                ", turbidity = " + input.getTurbidity() +
                " → Prediction: SAFE ✅";
    }

    public static class PredictionInput {
        private double ph;
        private double turbidity;

        public double getPh() {
            return ph;
        }

        public void setPh(double ph) {
            this.ph = ph;
        }

        public double getTurbidity() {
            return turbidity;
        }

        public void setTurbidity(double turbidity) {
            this.turbidity = turbidity;
        }
    }
}
