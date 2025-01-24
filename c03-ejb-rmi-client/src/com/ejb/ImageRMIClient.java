package com.ejb;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImageRMIClient {
    private final String host1;
    private final String host2;
    private final int port1;
    private final int port2;
    private ImageRMIInterface processor1;
    private ImageRMIInterface processor2;
    private static final int HEADER_SIZE = 54;

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
            if (imageData.length < HEADER_SIZE || imageData[0] != 0x42 || imageData[1] != 0x4D) {
                throw new Exception("Invalid BMP format");
            }

            ByteBuffer headerBuffer = ByteBuffer.wrap(imageData, 0, HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
            int width = headerBuffer.getInt(18);
            int height = headerBuffer.getInt(22);
            int bitsPerPixel = headerBuffer.getShort(28);
            int bytesPerPixel = bitsPerPixel / 8;

            int padding = (4 - ((width * bytesPerPixel) % 4)) % 4;
            int rowSize = width * bytesPerPixel + padding;
            int halfHeight = height / 2;
            
            int firstHalfSize = HEADER_SIZE + (rowSize * halfHeight);
            int secondHalfSize = HEADER_SIZE + (rowSize * (height - halfHeight));
            
            byte[] firstHalf = new byte[firstHalfSize];
            byte[] secondHalf = new byte[secondHalfSize];
            
            System.arraycopy(imageData, 0, firstHalf, 0, HEADER_SIZE);
            System.arraycopy(imageData, 0, secondHalf, 0, HEADER_SIZE);
            
            ByteBuffer firstHeader = ByteBuffer.wrap(firstHalf).order(ByteOrder.LITTLE_ENDIAN);
            firstHeader.putInt(22, halfHeight);
            
            ByteBuffer secondHeader = ByteBuffer.wrap(secondHalf).order(ByteOrder.LITTLE_ENDIAN);
            secondHeader.putInt(22, height - halfHeight);
            
            System.arraycopy(imageData, HEADER_SIZE, 
                           firstHalf, HEADER_SIZE, 
                           rowSize * halfHeight);

            System.arraycopy(imageData, HEADER_SIZE + (rowSize * halfHeight), 
                           secondHalf, HEADER_SIZE, 
                           rowSize * (height - halfHeight));
            
            System.out.println("Processing top half...");
            byte[] processedTop = processor1.zoomImage(firstHalf, zoomLevel, imageId + "_top");
            
            System.out.println("Processing bottom half...");
            byte[] processedBottom = processor2.zoomImage(secondHalf, zoomLevel, imageId + "_bottom");
            
            ByteBuffer processedHeader = ByteBuffer.wrap(processedTop, 0, HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
            int newWidth = processedHeader.getInt(18);
            int newRowSize = (newWidth * bytesPerPixel + ((4 - ((newWidth * bytesPerPixel) % 4)) % 4));
            int newHeight = processedHeader.getInt(22) + ByteBuffer.wrap(processedBottom, 0, HEADER_SIZE)
                                                                  .order(ByteOrder.LITTLE_ENDIAN)
                                                                  .getInt(22);
            
            int finalSize = HEADER_SIZE + (newRowSize * newHeight);
            byte[] result = new byte[finalSize];
            
            System.arraycopy(processedTop, 0, result, 0, HEADER_SIZE);
            ByteBuffer resultHeader = ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);
            resultHeader.putInt(22, newHeight);
            resultHeader.putInt(2, finalSize);
            
            System.arraycopy(processedTop, HEADER_SIZE, 
                           result, HEADER_SIZE, 
                           processedTop.length - HEADER_SIZE);
            
            System.arraycopy(processedBottom, HEADER_SIZE, 
                           result, HEADER_SIZE + (processedTop.length - HEADER_SIZE), 
                           processedBottom.length - HEADER_SIZE);

            System.out.println("\n=== Processing completed successfully ===");
            return result;

        } catch (Exception e) {
            System.err.println("Error during image processing: " + e.getMessage());
            throw e;
        }
    }
}