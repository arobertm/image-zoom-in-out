package com.imageservice.config;

public class AppConfig {
    private String brokerHost = "localhost";
    private String brokerPort = "61617";
    private int serverPort = 7000;

    public void loadFromEnvironment() {
        brokerHost = System.getenv().getOrDefault("BROKER_HOST", brokerHost);
        brokerPort = System.getenv().getOrDefault("BROKER_PORT", brokerPort);
        serverPort = Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", String.valueOf(serverPort)));
    }

    public String getBrokerHost() {
        return brokerHost;
    }

    public String getBrokerPort() {
        return brokerPort;
    }

    public int getServerPort() {
        return serverPort;
    }
}