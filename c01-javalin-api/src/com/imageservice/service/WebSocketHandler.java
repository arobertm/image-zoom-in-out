package com.imageservice.service;

import io.javalin.websocket.WsContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;

public class WebSocketHandler {
    private static final Map<String, WsContext> sessions = new ConcurrentHashMap<>();

    public static void configureSockets(io.javalin.Javalin app) {
        app.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                System.out.println("Client connected: " + ctx.getSessionId());
                sessions.put(ctx.getSessionId(), ctx);
            });

            ws.onMessage(ctx -> {
                try {
                  
                    JSONObject receivedMessage = new JSONObject(ctx.message());
                    String imageId = receivedMessage.getString("downloadUrl")
                                                  .replace("/api/images/", "");

           
                    JSONObject notification = new JSONObject();
                    notification.put("type", "imageProcessed");
                    notification.put("imageId", imageId);
                    notification.put("downloadUrl", "/api/images/" + imageId);
                    
                    System.out.println("Sending notification: " + notification.toString());
                    broadcastMessage(notification.toString());
                } catch (Exception e) {
                    System.err.println("Error processing message: " + e.getMessage());
                }
            });

            ws.onClose(ctx -> {
                System.out.println("Client disconnected: " + ctx.getSessionId());
                sessions.remove(ctx.getSessionId());
            });
        });
    }

    public static void broadcastMessage(String message) {
        sessions.values().forEach(session -> {
            try {
                session.send(message);
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        });
    }
}