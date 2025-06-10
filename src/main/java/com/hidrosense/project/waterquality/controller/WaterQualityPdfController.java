package com.hidrosense.project.waterquality.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.io.ByteArrayOutputStream;

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

        // Título y filtros con color y formato
        Paragraph titulo = new Paragraph("Reporte de Calidad de Agua", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.NORMAL, new BaseColor(33, 150, 243)));
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph("Generado: " + java.time.LocalDateTime.now(), FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, BaseColor.DARK_GRAY)));
        if (fecha != null) document.add(new Paragraph("Fecha: " + fecha));
        if (fecha_inicio != null && fecha_fin != null)
            document.add(new Paragraph("Rango: " + fecha_inicio + " a " + fecha_fin));
        if (location_id != null) document.add(new Paragraph("Location_ID: " + location_id));
        document.add(Chunk.NEWLINE);

        // Tabla colorida
        PdfPTable table = new PdfPTable(7); // Ajusta el número de columnas según lo que quieras mostrar
        table.setWidthPercentage(100);
        // Encabezados coloridos
        String[] headers = {"Fecha", "Location_ID", "pH", "E_coli", "Turbidez", "ICA_calculado", "Categoria_ICA"};
        BaseColor headerColor = new BaseColor(33, 150, 243);
        BaseColor headerTextColor = BaseColor.WHITE;
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, headerTextColor)));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        // Datos con filas alternadas
        boolean alternate = false;
        BaseColor rowColor = new BaseColor(232, 244, 253);
        for (JsonNode row : registros) {
            BaseColor bg = alternate ? rowColor : BaseColor.WHITE;
            table.addCell(cellWithBg(row.get("Date").asText(), bg));
            table.addCell(cellWithBg(row.get("Location_ID").asText(), bg));
            table.addCell(cellWithBg(row.get("pH").asText(), bg));
            table.addCell(cellWithBg(row.get("E_coli").asText(), bg));
            table.addCell(cellWithBg(row.get("Turbidez").asText(), bg));
            table.addCell(cellWithBg(row.get("ICA_calculado").asText(), bg));
            table.addCell(cellWithBg(row.get("Categoria_ICA").asText(), bg));
            alternate = !alternate;
        }
        document.add(table);

        document.add(Chunk.NEWLINE);

        // Gráfica: ICA_calculado por fecha (solo la fecha, sin la hora)
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (JsonNode row : registros) {
            double val = row.get("ICA_calculado").asDouble();
            String fechaCompleta = row.get("Date").asText();
            String soloFecha = fechaCompleta.split("T")[0]; // Solo la fecha, sin la hora
            dataset.addValue(val, "ICA", soloFecha);
            if (val < min) min = val;
            if (val > max) max = val;
        }
        JFreeChart chart = ChartFactory.createLineChart(
                "Evolución del ICA", "Fecha", "ICA_calculado", dataset
        );
        chart.setBackgroundPaint(Color.WHITE);

        // Colores vivos en la gráfica
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(232, 244, 253)); // Fondo azul claro
        plot.setDomainGridlinePaint(new Color(33, 150, 243)); // Líneas grid azules
        plot.setRangeGridlinePaint(new Color(33, 150, 243));
        ValueAxis yAxis = plot.getRangeAxis();
        if (min == max) {
            yAxis.setRange(min - 1, max + 1);
        } else {
            yAxis.setRange(Math.floor(min * 0.95), Math.ceil(max * 1.05));
        }

        // Ejes y etiquetas en color oscuro
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45); // Diagonal
        domainAxis.setTickLabelPaint(Color.DARK_GRAY);
        domainAxis.setLabelPaint(Color.DARK_GRAY);
        yAxis.setTickLabelPaint(Color.DARK_GRAY);
        yAxis.setLabelPaint(Color.DARK_GRAY);

        // Línea y puntos más vivos
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(33, 150, 243)); // Azul fuerte
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-4, -4, 8, 8));
        renderer.setSeriesShapesFilled(0, true);
        renderer.setSeriesOutlinePaint(0, Color.WHITE);
        plot.setRenderer(renderer);

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

    // Método auxiliar para celdas de tabla con fondo personalizado
    private PdfPCell cellWithBg(String text, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}
