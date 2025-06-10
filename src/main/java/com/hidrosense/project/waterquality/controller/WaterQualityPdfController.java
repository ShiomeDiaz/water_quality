package com.hidrosense.project.waterquality.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/api")
public class WaterQualityPdfController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/ica-report/pdf")
    public ResponseEntity<byte[]> getIcaReportPdf(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String fecha_inicio,
            @RequestParam(required = false) String fecha_fin,
            @RequestParam(required = false) Integer location_id
    ) throws Exception {

        // 1. Construir URL del microservicio Python
        StringBuilder url = new StringBuilder("http://localhost:3033/registros?");
        if (fecha != null) url.append("fecha=").append(fecha).append("&");
        if (fecha_inicio != null) url.append("fecha_inicio=").append(fecha_inicio).append("&");
        if (fecha_fin != null) url.append("fecha_fin=").append(fecha_fin).append("&");
        if (location_id != null) url.append("location_id=").append(location_id).append("&");
        if (url.charAt(url.length() - 1) == '&' || url.charAt(url.length() - 1) == '?') url.deleteCharAt(url.length() - 1);

        // 2. Consumir el microservicio
        String json = restTemplate.getForObject(url.toString(), String.class);

        // 3. Parsear el JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode registros = root.get("registros");

        // 4. Crear PDF en memoria
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        // Título y filtros
        document.add(new Paragraph("Reporte de Calidad de Agua", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        document.add(new Paragraph("Generado: " + java.time.LocalDateTime.now()));
        if (fecha != null) document.add(new Paragraph("Fecha: " + fecha));
        if (fecha_inicio != null && fecha_fin != null)
            document.add(new Paragraph("Rango: " + fecha_inicio + " a " + fecha_fin));
        if (location_id != null) document.add(new Paragraph("Location_ID: " + location_id));
        document.add(Chunk.NEWLINE);

        // Tabla
        PdfPTable table = new PdfPTable(7); // Ajusta el número de columnas según lo que quieras mostrar
        table.setWidthPercentage(100);
        // Encabezados
        String[] headers = {"Fecha", "Location_ID", "pH", "E_coli", "Turbidez", "ICA_calculado", "Categoria_ICA"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);
        }
        // Datos
        for (JsonNode row : registros) {
            table.addCell(row.get("Date").asText());
            table.addCell(row.get("Location_ID").asText());
            table.addCell(row.get("pH").asText());
            table.addCell(row.get("E_coli").asText());
            table.addCell(row.get("Turbidez").asText());
            table.addCell(row.get("ICA_calculado").asText());
            table.addCell(row.get("Categoria_ICA").asText());
        }
        document.add(table);

        document.add(Chunk.NEWLINE);

        // Gráfica: ICA_calculado por fecha
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (JsonNode row : registros) {
            dataset.addValue(row.get("ICA_calculado").asDouble(), "ICA", row.get("Date").asText());
        }
        JFreeChart chart = ChartFactory.createLineChart(
                "Evolución del ICA", "Fecha", "ICA_calculado", dataset
        );
        chart.setBackgroundPaint(Color.white);

        // Convertir gráfica a imagen
        ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
        int width = 500, height = 300;
        EncoderUtil.writeBufferedImage(chart.createBufferedImage(width, height), ImageFormat.PNG, chartBaos);

        com.itextpdf.text.Image chartImage = com.itextpdf.text.Image.getInstance(chartBaos.toByteArray());

        chartImage.setAlignment(Image.ALIGN_CENTER);
        document.add(chartImage);

        document.close();

        // 5. Retornar PDF como descarga
        HttpHeaders headersHttp = new HttpHeaders();
        headersHttp.setContentType(MediaType.APPLICATION_PDF);
        headersHttp.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_ica.pdf\"");
        return ResponseEntity.ok()
                .headers(headersHttp)
                .body(baos.toByteArray());
    }
}
