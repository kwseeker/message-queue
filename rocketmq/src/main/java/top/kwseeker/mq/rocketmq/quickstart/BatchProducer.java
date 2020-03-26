package top.kwseeker.mq.rocketmq.quickstart;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * 可以一次批量发送多个消息，消息大小默认不能超过4Ｍ
 */
public class BatchProducer {

    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("quickstart-batch-producer");
        producer.setNamesrvAddr(Config.NAMESRV_ADDR);
        producer.start();

        List<Message> messages = new ArrayList<>();
        messages.add(new Message(Config.TOPIC_NAME, "TagA", "OrderID001", "Hello world batch msg 0".getBytes()));
        messages.add(new Message(Config.TOPIC_NAME, "TagA", "OrderID002", "Hello world batch msg 1".getBytes()));
        messages.add(new Message(Config.TOPIC_NAME, "TagA", "OrderID003", "Hello world batch msg 2".getBytes()));
        producer.send(messages);

        producer.shutdown();
    }
}
