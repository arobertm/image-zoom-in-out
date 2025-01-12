package com.ejb;

import jakarta.ejb.MessageDriven;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.BytesMessage;

@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(
            propertyName = "destinationType",
            propertyValue = "jakarta.jms.Topic"),
        @ActivationConfigProperty(
            propertyName = "destination",
            propertyValue = "jms/topic/imageprocessing")
    })
public class ImageProcessingMDB implements MessageListener {
    private ImageRMIClient rmiClient;

    public ImageProcessingMDB() {
        try {
            rmiClient = new ImageRMIClient("localhost", 1099, "localhost", 1100);
            rmiClient.initialize();
            System.out.println("ImageProcessingMDB initialized with RMI Client");
        } catch (Exception e) {
            System.err.println("Error initializing RMI Client: " + e.getMessage());
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

                System.out.println("MDB received image - ID: " + imageId +
                    ", Size: " + imageData.length +
                    ", Zoom: " + zoomLevel);

                processImage(imageData, zoomLevel, imageId);
            }
        } catch (Exception e) {
            System.err.println("Error in MDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processImage(byte[] imageData, double zoomLevel, String imageId) {
        try {
            System.out.println("Processing image: " + imageId + " with zoom level: " + zoomLevel);
            
            byte[] processedImage = rmiClient.processImage(imageData, zoomLevel, imageId);
            
            System.out.println("Image processed successfully. Original size: " + imageData.length + 
                             " bytes, New size: " + processedImage.length + " bytes");

            // TODO: Add code to save to database and notify frontend
            
        } catch (Exception e) {
            System.err.println("Error processing image: " + e.getMessage());
            e.printStackTrace();
        }
    }
}