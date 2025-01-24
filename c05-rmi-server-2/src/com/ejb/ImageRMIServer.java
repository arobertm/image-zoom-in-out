package com.ejb;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.lang.management.MemoryMXBean;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class ImageRMIServer extends UnicastRemoteObject implements ImageRMIInterface {
    private final String serverId;
    private final OperatingSystemMXBean osBean;
    private final MemoryMXBean memoryBean;
    private final Snmp snmp;
    private final CommunityTarget target;
    private static final int HEADER_SIZE = 54;

    public ImageRMIServer(String serverId, String snmpManagerIp) throws Exception {
        super();
        this.serverId = serverId;
        this.osBean = ManagementFactory.getOperatingSystemMXBean();
        this.memoryBean = ManagementFactory.getMemoryMXBean();

        TransportMapping transport = new DefaultUdpTransportMapping();
        this.snmp = new Snmp(transport);
        transport.listen();

        this.target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version2c);
        target.setAddress(new UdpAddress(snmpManagerIp + "/161"));
        target.setRetries(2);
        target.setTimeout(1500);

        System.out.println("RMI Server initialized with SNMP to manager: " + snmpManagerIp);
    }

    private void sendMetrics() {
        try {
            System.out.println("\nCollecting metrics...");
            
            double cpuLoad = -1;
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                cpuLoad = ((com.sun.management.OperatingSystemMXBean) osBean).getCpuLoad() * 100;
            }

            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            double memoryUsage = ((double)(totalMemory - freeMemory) / totalMemory) * 100;

            System.out.println("Server ID: " + serverId);
            System.out.println("OS: " + System.getProperty("os.name"));
            System.out.println("CPU Usage: " + cpuLoad + "%");
            System.out.println("Memory Usage: " + memoryUsage + "%");

            PDU pdu = new PDU();
            pdu.setType(PDU.TRAP);

            pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.12345.1.1"), 
                    new OctetString(serverId)));
            pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.12345.1.2"), 
                    new OctetString(System.getProperty("os.name"))));
            pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.12345.1.3"), 
                    new Integer32((int)cpuLoad)));
            pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.12345.1.4"), 
                    new Integer32((int)memoryUsage)));

            System.out.println("Sending SNMP trap to " + target.getAddress());

            ResponseEvent response = snmp.send(pdu, target);
            
            if (response != null) {
                if (response.getError() != null) {
                    System.err.println("Error sending SNMP: " + response.getError().toString());
                } else {
                    PDU responsePDU = response.getResponse();
                    if (responsePDU != null) {
                        System.out.println("SNMP metrics sent successfully. Response type: " + 
                            responsePDU.getType());
                    } else {
                        System.out.println("SNMP metrics sent but no response received");
                    }
                }
            } else {
                System.out.println("SNMP metrics sent (no response expected for traps)");
            }

        } catch (Exception e) {
            System.err.println("Error sending SNMP metrics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public byte[] zoomImage(byte[] imageData, double zoomLevel, String imageId) throws RemoteException {
        try {
            System.out.println("Processing image on server " + serverId);
            byte[] processedImage = processImageData(imageData, zoomLevel, imageId);
            sendMetrics();
            
            return processedImage;
            
        } catch (Exception e) {
            System.err.println("Error processing image: " + e.getMessage());
            throw new RemoteException("Image processing failed", e);
        }
    }

    private byte[] processImageData(byte[] imageData, double zoomLevel, String imageId) throws RemoteException {
        try {
            System.out.println("Processing image part: " + imageId);
            
            if (imageData.length < HEADER_SIZE || imageData[0] != 0x42 || imageData[1] != 0x4D) {
                throw new RemoteException("Invalid BMP format");
            }

            ByteBuffer headerBuffer = ByteBuffer.wrap(imageData, 0, HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
            int fileSize = headerBuffer.getInt(2);
            int dataOffset = headerBuffer.getInt(10);
            int width = headerBuffer.getInt(18);
            int height = headerBuffer.getInt(22);
            int bitsPerPixel = headerBuffer.getShort(28);
            
            System.out.println("Original dimensions: " + width + "x" + height);

            int newWidth = (int)(width * zoomLevel);
            int newHeight = (int)(height * zoomLevel);
            
            System.out.println("New dimensions: " + newWidth + "x" + newHeight);

            int padding = (4 - ((width * 3) % 4)) % 4;
            int newPadding = (4 - ((newWidth * 3) % 4)) % 4;
            int newDataSize = ((newWidth * 3 + newPadding) * newHeight);
            int newFileSize = HEADER_SIZE + newDataSize;
            byte[] result = new byte[newFileSize];

            System.arraycopy(imageData, 0, result, 0, HEADER_SIZE);
            ByteBuffer resultBuffer = ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);
            resultBuffer.putInt(2, newFileSize);
            resultBuffer.putInt(18, newWidth);
            resultBuffer.putInt(22, newHeight);

            for (int y = 0; y < newHeight; y++) {
                for (int x = 0; x < newWidth; x++) {
                    int srcX = (int)(x / zoomLevel);
                    int srcY = (int)(y / zoomLevel);
                    
                    srcX = Math.min(srcX, width - 1);
                    srcY = Math.min(srcY, height - 1);

                    int newPos = HEADER_SIZE + (y * (newWidth * 3 + newPadding)) + (x * 3);
                    int oldPos = HEADER_SIZE + (srcY * (width * 3 + padding)) + (srcX * 3);

                    if (newPos + 2 < result.length && oldPos + 2 < imageData.length) {
                        result[newPos] = imageData[oldPos];        
                        result[newPos + 1] = imageData[oldPos + 1];
                        result[newPos + 2] = imageData[oldPos + 2];
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
        if (args.length != 3) {
            System.out.println("Usage: java ImageRMIServer <serverId> <rmiPort> <snmpManagerIp>");
            System.exit(1);
        }

        try {
            String serverId = args[0];
            int rmiPort = Integer.parseInt(args[1]);
            String snmpManagerIp = args[2];

            ImageRMIServer server = new ImageRMIServer(serverId, "nodejs-app");
            Registry registry = LocateRegistry.createRegistry(rmiPort);
            registry.rebind("ImageProcessor", server);

            System.out.println("RMI Server " + serverId + " running on port " + rmiPort);
            System.out.println("SNMP sending to manager at nodejs-app");

        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}