package com.main;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

public class JMSPublisher {

    public static boolean publishMessage(byte[] imageBytes, double scaleFactor, String zoomType) {
        try {
            Properties props = Config.getJmsProperties();

            Context context = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("ConnectionFactory");
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic topic = (Topic) context.lookup(Config.JMS_TOPIC_NAME);
            
            BytesMessage message = session.createBytesMessage();
            message.writeBytes(imageBytes);
            message.setDoubleProperty("scaleFactor", scaleFactor);
            message.setStringProperty("zoomType", zoomType);

            MessageProducer producer = session.createProducer(topic);
            producer.send(message);

            connection.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
