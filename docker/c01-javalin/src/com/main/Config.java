package com.main;

import java.util.Properties;

public class Config {
    public static final String JMS_TOPIC_NAME = "imageTopic";

    public static Properties getJmsProperties() {
        Properties props = new Properties();
        props.put("java.naming.factory.initial", "org.apache.openejb.client.LocalInitialContextFactory");
        props.put("java.naming.provider.url", "tcp://localhost:61616");
        props.put("topic." + JMS_TOPIC_NAME, JMS_TOPIC_NAME);
        return props;
    }
}
