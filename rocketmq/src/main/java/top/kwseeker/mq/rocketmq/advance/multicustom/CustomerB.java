package top.kwseeker.mq.rocketmq.advance.multicustom;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import top.kwseeker.mq.rocketmq.Config;

import java.util.List;

public class CustomerB {

    public static void main(String[] args) throws Exception {
        //DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("customer-ab");    //不正确的做法（不要让同一个组的消费者消费不同topic消息）
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("customer-b");
        consumer.setNamesrvAddr(Config.NAMESRV_ADDR);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        consumer.subscribe("topic-b", "*");
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
        System.out.printf("ConsumerB Started.%n");
    }
}
