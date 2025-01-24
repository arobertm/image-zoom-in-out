package com.imageservice.service;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class JMSPublisher {
    private final String brokerUrl;
    private Connection connection;
    private Session session;

    public JMSPublisher(String brokerHost, String brokerPort) {
        this.brokerUrl = "tcp://" + brokerHost + ":" + brokerPort;
    }

    public void initialize() throws Exception {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            
            connection = factory.createConnection();
            connection.setClientID("ImageServicePublisher");
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();
            
            System.out.println("Connected to broker at: " + brokerUrl);
        } catch (JMSException e) {
            System.err.println("Error initializing JMS: " + e.getMessage());
            throw e;
        }
    }

    public void sendBinaryMessage(String topicName, byte[] imageData, double zoomLevel, String imageId) throws JMSException {
        Topic topic = null;
        MessageProducer producer = null;
        
        try {
            topic = session.createTopic(topicName);
            producer = session.createProducer(topic);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            BytesMessage message = session.createBytesMessage();
            message.writeBytes(imageData);
            message.setDoubleProperty("zoomLevel", zoomLevel);
            message.setStringProperty("imageId", imageId);

            producer.send(message);
            System.out.println("Sent binary message - ID: " + imageId + ", Zoom Level: " + zoomLevel);
            
        } finally {
            if (producer != null) {
                producer.close();
            }
        }
    }

    public void cleanup() {
        try {
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}