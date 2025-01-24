package com.imageservice;

import com.imageservice.config.AppConfig;
import com.imageservice.service.ImageProcessor;
import com.imageservice.service.JMSPublisher;
import com.imageservice.service.WebSocketHandler;
import io.javalin.Javalin;

public class Main {
    private static JMSPublisher publisher;
    private static Javalin app;

    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        config.loadFromEnvironment();

        try {
            publisher = new JMSPublisher(config.getBrokerHost(), config.getBrokerPort());
            publisher.initialize();
            
            startJavalin(config.getServerPort());
            setupShutdownHook();
            
            System.out.println("Service started on port " + config.getServerPort());
        } catch (Exception e) {
            System.err.println("Error starting service: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void startJavalin(int port) {
        app = Javalin.create(config -> {
        	config.showJavalinBanner = true;
            config.plugins.enableCors(cors -> cors.add(it -> {
                it.anyHost();
                it.allowCredentials = true;
            }));
            config.http.maxRequestSize = 10 * 1024 * 1024;
        });

        app.get("/health", ctx -> ctx.result("OK"));
        ImageProcessor.configureRoutes(app, publisher);
        WebSocketHandler.configureSockets(app);

        app.start(port);
    }

    private static void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down services...");
            if (publisher != null) {
                publisher.cleanup();
            }
            if (app != null) {
                app.stop();
            }
        }));
    }
}