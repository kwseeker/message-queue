package top.kwseeker.mq.rocketmq.quickstart;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.List;

/**
 * 广播消费只需要将消费者模式设置成消息广播
 */
public class BroadcastConsumer {

    public static class ConsumeTask implements Runnable {

        private String consumerName;

        public ConsumeTask(String consumerName) {
            this.consumerName = consumerName;
        }

        @Override
        public void run() {
            try {
                DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerName);
                consumer.setNamesrvAddr(Config.NAMESRV_ADDR);
                consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
                consumer.setMessageModel(MessageModel.BROADCASTING);
                consumer.subscribe("broadcast-topic", "TagA || TagC || TagD");

                consumer.registerMessageListener(new MessageListenerConcurrently() {
                    @Override
                    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                                    ConsumeConcurrentlyContext context) {
                        System.out.printf(Thread.currentThread().getName() + " Receive New Messages: " + msgs + "%n");
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                });
                consumer.start();
                System.out.println("Thread " + Thread.currentThread().getName() + " started");
            } catch (MQClientException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Thread thread1 = new Thread(new ConsumeTask("broadcast-consumer1"));
        Thread thread2 = new Thread(new ConsumeTask("broadcast-consumer2"));
        thread1.start();
        thread2.start();

        System.out.printf("Broadcast Consumers Started.%n");
    }
}
