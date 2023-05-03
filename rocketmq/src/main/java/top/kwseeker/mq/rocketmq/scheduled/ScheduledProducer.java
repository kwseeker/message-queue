package top.kwseeker.mq.rocketmq.scheduled;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import top.kwseeker.mq.rocketmq.Config;

public class ScheduledProducer {

    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("scheduled-producer");
        producer.setNamesrvAddr(Config.NAMESRV_ADDR);
        producer.start();
        for (int i = 0; i < 10; i++) {
            Message message = new Message(Config.TOPIC_NAME, ("Hello scheduled message " + i).getBytes());
            //先将消息发到延时队列，到时间后再从延时队列发送到目标队列
            //延迟10s
            message.setDelayTimeLevel(3);
            producer.send(message);
        }

        producer.shutdown();
    }
}
