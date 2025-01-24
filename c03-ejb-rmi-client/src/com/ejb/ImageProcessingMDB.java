package com.ejb;

import jakarta.ejb.MessageDriven;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.BytesMessage;
import jakarta.websocket.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(
            propertyName = "destinationType",
            propertyValue = "jakarta.jms.Topic"),
        @ActivationConfigProperty(
            propertyName = "destination",
            propertyValue = "jms/topic/imageprocessing")
    })
@ClientEndpoint
public class ImageProcessingMDB implements MessageListener {
    private ImageRMIClient rmiClient;
    private final HttpClient httpClient;
    private static final String NODE_SERVER_URL = "http://localhost:3000/api/images";
    private WebSocketSession webSocketSession;

    public ImageProcessingMDB() {
        try {
            rmiClient = new ImageRMIClient("localhost", 1099, "localhost", 1100);
            rmiClient.initialize();
            httpClient = HttpClient.newHttpClient();
            connectWebSocket();
            System.out.println("ImageProcessingMDB initialized");
        } catch (Exception e) {
            System.err.println("Error initializing: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void connectWebSocket() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String wsUrl = "ws://localhost:7000/ws";
            webSocketSession = new WebSocketSession(container, new URI(wsUrl));
            System.out.println("WebSocket connected to Javalin");
        } catch (Exception e) {
            System.err.println("Error connecting to WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof BytesMessage) {
                BytesMessage bMsg = (BytesMessage) message;
                
                double zoomLevel = bMsg.getDoubleProperty("zoomLevel");
                String imageId = bMsg.getStringProperty("imageId");

                byte[] imageData = new byte[(int) bMsg.getBodyLength()];
                bMsg.readBytes(imageData);

                System.out.println("Processing image: " + imageId);
                processImage(imageData, zoomLevel, imageId);
            }
        } catch (Exception e) {
            System.err.println("Error in MDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processImage(byte[] imageData, double zoomLevel, String imageId) {
        try {
            byte[] processedImage = rmiClient.processImage(imageData, zoomLevel, imageId);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(NODE_SERVER_URL))
                .header("Content-Type", "application/octet-stream")
                .header("Image-ID", imageId)
                .header("Zoom-Level", String.valueOf(zoomLevel))
                .POST(HttpRequest.BodyPublishers.ofByteArray(processedImage))
                .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Image stored in Node.js successfully");
                notifyProcessingComplete(imageId, response.body());
            } else {
                throw new RuntimeException("Failed to store image: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error processing image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void notifyProcessingComplete(String imageId, String nodeResponse) {
        try {
            if (webSocketSession != null && webSocketSession.isConnected()) {
                webSocketSession.sendMessage(nodeResponse);
                System.out.println("WebSocket notification sent for image: " + imageId);
            } else {
                System.err.println("WebSocket not connected, trying to reconnect...");
                connectWebSocket();
                if (webSocketSession != null && webSocketSession.isConnected()) {
                    webSocketSession.sendMessage(nodeResponse);
                    System.out.println("WebSocket notification sent after reconnection");
                }
            }
            System.out.println("Processing complete for image: " + imageId + 
                             ", response: " + nodeResponse);
        } catch (Exception e) {
            System.err.println("Error sending WebSocket notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}