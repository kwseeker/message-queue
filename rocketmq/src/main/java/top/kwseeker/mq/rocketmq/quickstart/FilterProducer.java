package top.kwseeker.mq.rocketmq.quickstart;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息过滤
 * 生产者只需要在消息中添加一些用于过滤的属性（添加到properties中）
 */
public class FilterProducer {

    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("quickstart-filter-producer");
        producer.setNamesrvAddr(Config.SINGLE_NAMESRV_ADDR);
        producer.start();

        List<Message> messages = new ArrayList<>();
        Message message = new Message(Config.TOPIC_NAME, "TagA", "key1",
                "Hello Rocket filter message1".getBytes());
        //设置用于过滤的属性
        message.putUserProperty("a", "1");
        messages.add(message);
        Message message1 = new Message(Config.TOPIC_NAME, "TagB", "key2",
                "Hello Rocket filter message2".getBytes());
        message1.putUserProperty("a", "5");
        messages.add(message1);

        SendResult result = producer.send(messages);
        System.out.println("send result: " + result.toString());

        producer.shutdown();
    }
}
