package top.kwseeker.mq.rocketmq.filter;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import top.kwseeker.mq.rocketmq.Config;

import java.util.List;

/**
 * 消息过滤
 *
 * 比较粗略的过滤可以使用tags实现
 *
 * 细粒度的过滤通过订阅消息时指定 MessageSelector 实现
 *  需要添加配置 enablePropertyFilter=true
 *
 * RocketMQ only defines some basic grammars to support this feature. You could also extend it easily.
 *
 * Numeric comparison, like >, >=, <, <=, BETWEEN, =;
 * Character comparison, like =, <>, IN;
 * IS NULL or IS NOT NULL;
 * Logical AND, OR, NOT;
 * Constant types are:
 *
 * Numeric, like 123, 3.1415;
 * Character, like ‘abc’, must be made with single quotes;
 * NULL, special constant;
 * Boolean, TRUE or FALSE;
 */
public class FilterConsumer {

    public static void main(String[] args) throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("quickstart-filter-consumer");
        consumer.setNamesrvAddr(Config.SINGLE_NAMESRV_ADDR);

        //提供MessageSelector定义消息过滤规则
        //consumer.subscribe(Config.TOPIC_NAME, MessageSelector.bySql("a = 1"));
        //consumer.subscribe(Config.TOPIC_NAME, MessageSelector.byTag("TagB"));
        consumer.subscribe(Config.TOPIC_NAME, MessageSelector.bySql("a between 0 and 1.5"));

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                for (MessageExt msg : msgs) {
                    System.out.println(new String(msg.getBody()));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
    }
}
