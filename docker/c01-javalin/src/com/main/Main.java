package com.main;

import io.javalin.Javalin;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);

        app.post("/zoom", ctx -> {
            String scaleFactorParam = ctx.formParam("scaleFactor");
            String zoomType = ctx.formParam("zoomType");

            if (scaleFactorParam == null || zoomType == null || ctx.uploadedFile("image") == null) {
                ctx.status(400).result("Scale factor, zoom type, and image are required!");
                return;
            }

            String contentType = ctx.uploadedFile("image").contentType();
            if (!"image/bmp".equalsIgnoreCase(contentType)) {
                ctx.status(400).result("Invalid file type! Only BMP images are supported.");
                return;
            }

            InputStream imageInputStream = ctx.uploadedFile("image").content();

            double scaleFactor;
            try {
                scaleFactor = Double.parseDouble(scaleFactorParam);
            } catch (NumberFormatException e) {
                ctx.status(400).result("Invalid scale factor format!");
                return;
            }
            if ("in".equals(zoomType)) {
                if (!ImageProcessor.isValidZoomInFactor(scaleFactor)) {
                    ctx.status(400).result("Invalid scale factor for zoom in!");
                    return;
                }
            } else if ("out".equals(zoomType)) {
                if (!ImageProcessor.isValidZoomOutFactor(scaleFactor)) {
                    ctx.status(400).result("Invalid scale factor for zoom out!");
                    return;
                }
            } else {
                ctx.status(400).result("Invalid zoom type! Use 'in' or 'out'.");
                return;
            }

            byte[] imageBytes = IOUtils.toByteArray(imageInputStream);

            boolean success = JMSPublisher.publishMessage(imageBytes, scaleFactor, zoomType);

            if (success) {
                ctx.status(200).result("Zoom request successfully sent to JMS Broker!");
            } else {
                ctx.status(500).result("Failed to send zoom request to JMS Broker.");
            }
        });
        
        app.get("/test-broker", ctx -> {
            String testMessage = "Test message to JMS Broker";
            boolean success = JMSPublisher.publishMessage(testMessage.getBytes(), 1.0, "test");

            if (success) {
                ctx.status(200).result("Test message successfully sent to JMS Broker!");
            } else {
                ctx.status(500).result(" --> GET: Failed to send test message to JMS Broker.");
            }
        });
    }
}
