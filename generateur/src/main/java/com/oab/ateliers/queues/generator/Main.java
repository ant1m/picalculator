package com.oab.ateliers.queues.generator;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

public class Main {

    private static final String QUEUE_NAME = "points";
    private static final Integer NUMBER_OF_POINTS = 1000000000;
    private static final RandomPointsGenerator generator = new RandomPointsGenerator();

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = null;
        Channel channel = null;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            for (int i = 0; i<NUMBER_OF_POINTS;i++) {
                sendRandomPoint(channel);
            }
        } catch (IOException e) {
            System.out.println("Something went wrong while connecting and sending to broker;"+ e.getMessage());
        }
        finally {
            try {
                if (channel != null) channel.close();
                if (connection != null) connection.close();
            }
            catch (IOException e) {
                System.out.println("Something went wrong while closing connection and channel" + e.getMessage());
            }
        }
    }

    private static void sendRandomPoint(Channel channel) throws IOException {
        String message = generator.generate().toJson();
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
    }
}
