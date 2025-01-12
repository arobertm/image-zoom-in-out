package com.ejb;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImageRMIServer extends UnicastRemoteObject implements ImageRMIInterface {
    
    private static final int HEADER_SIZE = 54;
    
    public ImageRMIServer() throws RemoteException {
        super();
    }

    @Override
    public byte[] zoomImage(byte[] imageData, double zoomLevel, String imageId) throws RemoteException {
        try {
            System.out.println("Processing image part: " + imageId);
            
            // Verificăm dacă avem header BMP valid
            if (imageData.length < HEADER_SIZE || imageData[0] != 0x42 || imageData[1] != 0x4D) {
                throw new RemoteException("Invalid BMP format");
            }

            // Citim informațiile din header
            ByteBuffer headerBuffer = ByteBuffer.wrap(imageData, 0, HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
            int fileSize = headerBuffer.getInt(2);
            int dataOffset = headerBuffer.getInt(10);
            int width = headerBuffer.getInt(18);
            int height = headerBuffer.getInt(22);
            int bitsPerPixel = headerBuffer.getShort(28);
            
            System.out.println("Original dimensions: " + width + "x" + height);

            // Calculăm noile dimensiuni
            int newWidth = (int)(width * zoomLevel);
            int newHeight = (int)(height * zoomLevel);
            
            System.out.println("New dimensions: " + newWidth + "x" + newHeight);

            // Calculăm padding pentru alinierea la 4 bytes
            int padding = (4 - ((width * 3) % 4)) % 4;
            int newPadding = (4 - ((newWidth * 3) % 4)) % 4;

            // Calculăm dimensiunea noilor date
            int newDataSize = ((newWidth * 3 + newPadding) * newHeight);
            int newFileSize = HEADER_SIZE + newDataSize;

            // Creăm noul array pentru imaginea procesată
            byte[] result = new byte[newFileSize];

            // Copiem și actualizăm header-ul
            System.arraycopy(imageData, 0, result, 0, HEADER_SIZE);
            ByteBuffer resultBuffer = ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);
            resultBuffer.putInt(2, newFileSize);
            resultBuffer.putInt(18, newWidth);
            resultBuffer.putInt(22, newHeight);

            // Procesăm datele imaginii
            for (int y = 0; y < newHeight; y++) {
                for (int x = 0; x < newWidth; x++) {
                    int srcX = (int)(x / zoomLevel);
                    int srcY = (int)(y / zoomLevel);
                    
                    // Asigurăm că nu depășim limitele
                    srcX = Math.min(srcX, width - 1);
                    srcY = Math.min(srcY, height - 1);

                    // Calculăm pozițiile în arrays
                    int newPos = HEADER_SIZE + (y * (newWidth * 3 + newPadding)) + (x * 3);
                    int oldPos = HEADER_SIZE + (srcY * (width * 3 + padding)) + (srcX * 3);

                    // Copiem valorile BGR
                    if (newPos + 2 < result.length && oldPos + 2 < imageData.length) {
                        result[newPos] = imageData[oldPos];        // Blue
                        result[newPos + 1] = imageData[oldPos + 1];// Green
                        result[newPos + 2] = imageData[oldPos + 2];// Red
                    }
                }
            }

            System.out.println("Processed image part: " + imageId + 
                             " Size before: " + imageData.length + 
                             " Size after: " + result.length);
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Error processing image: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Image processing failed", e);
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("Usage: java ImageRMIServer <host> <port>");
                System.exit(1);
            }
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            
            System.setProperty("java.rmi.server.hostname", host);
            
            ImageRMIServer server = new ImageRMIServer();
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("ImageProcessor", server);
            System.out.println("RMI Server is running on " + host + ":" + port);
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}