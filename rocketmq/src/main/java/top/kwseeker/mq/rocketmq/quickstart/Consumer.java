package top.kwseeker.mq.rocketmq.quickstart;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

public class Consumer {

    public static void main(String[] args) throws Exception {

        //Consumer Group 用来表示一个消费消息应用,一个 Consumer Group 下包含多个 Consumer 实例,可以是多台机器,也可
        //以是多个进程,或者是一个进程的多个 Consumer 对象。一个 Consumer Group 下的多个 Consumer 以均摊
        //方式消费消息,如果设置为广播方式,那么这个 Consumer Group 下的每个实例都消费全量数据。
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(Config.CONSUMER_NAME);
        consumer.setNamesrvAddr(Config.NAMESRV_ADDR);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        consumer.subscribe(Config.TOPIC_NAME, "*");
        // Register callback to execute on arrival of messages fetched from brokers.
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                            ConsumeConcurrentlyContext context) {
                System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
                for (MessageExt me : msgs) {
                    System.out.println("message: " + new String(me.getBody()));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
        System.out.printf("Consumer Started.%n");
    }
}
