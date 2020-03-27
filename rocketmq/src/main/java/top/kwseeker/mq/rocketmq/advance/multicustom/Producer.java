package top.kwseeker.mq.rocketmq.advance.multicustom;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import top.kwseeker.mq.rocketmq.quickstart.Config;

public class Producer {

    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(Config.PRODUCER_NAME);
        producer.setNamesrvAddr(Config.NAMESRV_ADDR);
        producer.start();

        //发送10个"topic-A"消息
        for (int i = 0; i < 10; i++) {
            Message msg = new Message("topic-a", "TagA", ("topic-a message " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            SendResult sendResult = producer.send(msg);
            System.out.printf("send result: %s%n", sendResult);
        }
        //发送10个"topic-B"消息
        for (int i = 0; i < 10; i++) {
            Message msg = new Message("topic-b", "TagB", ("topic-b message " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            SendResult sendResult = producer.send(msg);
            System.out.printf("send result: %s%n", sendResult);
        }

        producer.shutdown();
    }
}
