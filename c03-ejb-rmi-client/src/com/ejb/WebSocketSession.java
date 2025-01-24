package com.ejb;

import jakarta.websocket.*;
import java.net.URI;
import java.io.IOException;

@ClientEndpoint
public class WebSocketSession {
    private Session session;
    private boolean connected = false;

    public WebSocketSession(WebSocketContainer container, URI endpointURI) throws Exception {
        try {
            container.setDefaultMaxTextMessageBufferSize(1024 * 1024);
            session = container.connectToServer(this, endpointURI);
            connected = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("WebSocket connection opened");
        this.session = session;
        this.connected = true;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket connection closed: " + reason);
        this.connected = false;
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
        this.connected = false;
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received message from server: " + message);
    }

    public void sendMessage(String message) throws IOException {
        if (session != null && session.isOpen()) {
            System.out.println("Sending WebSocket message: " + message);
            session.getBasicRemote().sendText(message);
        } else {
            throw new IOException("WebSocket session is not open");
        }
    }

    public boolean isConnected() {
        return connected && session != null && session.isOpen();
    }

    public void close() throws IOException {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}