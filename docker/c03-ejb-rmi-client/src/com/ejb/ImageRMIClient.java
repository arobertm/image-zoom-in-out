package com.ejb;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ImageRMIClient {
    private final String host1;
    private final String host2;
    private final int port1;
    private final int port2;
    private ImageRMIInterface processor1;
    private ImageRMIInterface processor2;

    public ImageRMIClient(String host1, int port1, String host2, int port2) {
        this.host1 = host1;
        this.host2 = host2;
        this.port1 = port1;
        this.port2 = port2;
    }

    public void initialize() throws Exception {
        try {
            Registry registry1 = LocateRegistry.getRegistry(host1, port1);
            processor1 = (ImageRMIInterface) registry1.lookup("ImageProcessor");
            System.out.println("Connected to RMI Server 1 at " + host1 + ":" + port1);

            Registry registry2 = LocateRegistry.getRegistry(host2, port2);
            processor2 = (ImageRMIInterface) registry2.lookup("ImageProcessor");
            System.out.println("Connected to RMI Server 2 at " + host2 + ":" + port2);

        } catch (Exception e) {
            System.err.println("RMI Client initialization failed: " + e.getMessage());
            throw e;
        }
    }

    public byte[] processImage(byte[] imageData, double zoomLevel, String imageId) throws Exception {
        try {
            int halfLength = imageData.length / 2;
            byte[] firstHalf = new byte[halfLength];
            byte[] secondHalf = new byte[imageData.length - halfLength];
            
            System.arraycopy(imageData, 0, firstHalf, 0, halfLength);
            System.arraycopy(imageData, halfLength, secondHalf, 0, imageData.length - halfLength);
            
            byte[] processedPart1 = processor1.zoomImage(firstHalf, zoomLevel, imageId + "_part1");
            byte[] processedPart2 = processor2.zoomImage(secondHalf, zoomLevel, imageId + "_part2");

            byte[] result = new byte[processedPart1.length + processedPart2.length];
            System.arraycopy(processedPart1, 0, result, 0, processedPart1.length);
            System.arraycopy(processedPart2, 0, result, processedPart1.length, processedPart2.length);
            System.out.println("\n=== Processing completed successfully ===");
            return result;
        } catch (Exception e) {
            System.err.println("Error during image processing: " + e.getMessage());
            throw e;
        }
    }
}