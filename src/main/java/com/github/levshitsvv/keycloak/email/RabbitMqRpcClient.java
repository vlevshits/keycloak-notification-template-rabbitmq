package com.github.levshitsvv.keycloak.email;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class RabbitMqRpcClient implements AutoCloseable {

    private final Channel channel;
    private final String replyQueueName;

    public RabbitMqRpcClient(Channel channel) throws IOException {
        this.channel = channel;
        // Create a unique callback queue for this client instance
        this.replyQueueName = channel.queueDeclare().getQueue();
    }

    public String call(String exchange, String routingKey, String message, String type, int timeoutMs)
            throws IOException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .contentType("application/json")
                .contentEncoding("UTF-8")
                .type(type)
                .build();

        channel.basicPublish(exchange, routingKey, props, message.getBytes(StandardCharsets.UTF_8));

        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

        String ctag = channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                    byte[] body) throws IOException {
                if (properties.getCorrelationId().equals(corrId)) {
                    response.offer(new String(body, StandardCharsets.UTF_8));
                }
            }
        });

        String result = response.poll(timeoutMs, TimeUnit.MILLISECONDS);
        channel.basicCancel(ctag);

        if (result == null) {
            throw new IOException("RPC timeout after " + timeoutMs + "ms");
        }

        return result;
    }

    @Override
    public void close() throws Exception {
        // Optimization: we could keep the channel open if the factory manages it
        // but for now, we follow the interface.
    }
}
