package com.oab.ateliers.queues.calculator;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;

public class Main {
    private static final String EXCHANGE_NAME = "resultsExchange";

    private static int nbIn = 0;
    private static int nbTotal = 0;

    public static void main(String[] argv) throws IOException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume("results", true, "", true, false, null, consumer);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            if (Boolean.valueOf(message)) nbIn++;
            nbTotal++;
            if (nbTotal%10000 == 0) System.out.println(nbIn + "/"+nbTotal+" > PI = " + (float)4 * (float)nbIn/(float)nbTotal);
        }
    }
}
