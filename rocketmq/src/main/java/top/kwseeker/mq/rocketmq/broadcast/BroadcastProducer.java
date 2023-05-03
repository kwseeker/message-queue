package top.kwseeker.mq.rocketmq.broadcast;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import top.kwseeker.mq.rocketmq.Config;

public class BroadcastProducer {

    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("broadcast-producer");
        producer.setNamesrvAddr(Config.NAMESRV_ADDR);
        //TODO:怎么用的
        //producer.createTopic("broadcast-key", "broadcast-topic", 4);
        producer.start();

        for (int i = 0; i < 100; i++){
            Message msg = new Message("broadcast-topic",
                    "TagA",
                    "OrderID188",
                    ("Hello world " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            SendResult sendResult = producer.send(msg);
            System.out.printf("%s%n", sendResult);
        }
        producer.shutdown();
    }
}
