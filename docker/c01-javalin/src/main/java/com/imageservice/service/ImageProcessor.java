package com.imageservice.service;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import java.util.Map;
import java.util.UUID;

public class ImageProcessor {
    private static final double[] ZOOM_LEVELS = {0.1, 0.2, 0.4, 0.6, 1.0, 2.0, 4.0, 6.0, 8.0, 16.0};

    public static void configureRoutes(Javalin app, JMSPublisher publisher) {
        app.post("/api/image/zoom", ctx -> handleImageZoom(ctx, publisher));
        app.get("/api/zoom-levels", ctx -> ctx.json(ZOOM_LEVELS));
    }

    private static void handleImageZoom(Context ctx, JMSPublisher publisher) {
        try {
            UploadedFile file = ctx.uploadedFile("image");
            if (file == null) {
                ctx.status(400).json(Map.of("error", "No image file provided"));
                return;
            }

            String zoomLevelStr = ctx.formParam("zoomLevel");
            if (zoomLevelStr == null) {
                ctx.status(400).json(Map.of("error", "No zoom level provided"));
                return;
            }

            double zoomLevel = Double.parseDouble(zoomLevelStr);
            if (!isValidZoomLevel(zoomLevel)) {
                ctx.status(400).json(Map.of("error", "Invalid zoom level",
                                          "validLevels", ZOOM_LEVELS));
                return;
            }

            byte[] imageBytes = file.content().readAllBytes();
            if (imageBytes.length < 2 || imageBytes[0] != 0x42 || imageBytes[1] != 0x4D) {
                ctx.status(400).json(Map.of("error", "Invalid BMP format"));
                return;
            }

            String imageId = UUID.randomUUID().toString();
            publisher.sendBinaryMessage("jms/topic/imageprocessing", imageBytes, zoomLevel, imageId);

            ctx.json(Map.of(
                "status", "success",
                "message", "Image sent for processing",
                "imageId", imageId,
                "zoomLevel", zoomLevel
            ));

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "Invalid zoom level format"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Internal server error",
                                      "message", e.getMessage()));
        }
    }

    private static boolean isValidZoomLevel(double level) {
        for (double validLevel : ZOOM_LEVELS) {
            if (Math.abs(level - validLevel) < 0.0001) return true;
        }
        return false;
    }
}