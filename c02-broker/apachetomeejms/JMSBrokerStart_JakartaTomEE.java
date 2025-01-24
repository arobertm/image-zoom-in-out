package apachetomeejms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

import org.apache.activemq.broker.BrokerService;

public class JMSBrokerStart_JakartaTomEE {
    public static void initBroker(String ip, String port) throws Exception {
        System.out.println("Initializing broker with:");
        System.out.println("IP: " + ip);
        System.out.println("Port: " + port);
        
        InetAddress inetAddress = InetAddress.getByName(ip);
        System.out.println("Resolved IP: " + inetAddress.getHostAddress());

        BrokerService broker = new BrokerService();
        broker.addConnector("tcp://" + inetAddress.getHostAddress() + ":" + port);
        
        broker.setPersistent(true);
        broker.setUseJmx(false); 
        
        broker.start();
        broker.waitUntilStarted();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: JMSBrokerStart_JakartaTomEE <ip> <port>");
            return;
        }

        try { 
            initBroker(args[0], args[1]); 
            
            while (true) {
                Thread.sleep(Long.MAX_VALUE);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}