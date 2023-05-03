package top.kwseeker.mq.rocketmq.ordered;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import top.kwseeker.mq.rocketmq.Config;

import java.util.List;

/**
 * 顺序消息
 */
public class OrderedProducer {

    public static void main(String[] args) throws Exception {

        DefaultMQProducer producer = new DefaultMQProducer("quickstart-ordered-producer");
        producer.setNamesrvAddr(Config.NAMESRV_ADDR);
        //producer.createTopic();
        producer.start();

        //TAGS是用于消息过滤的
        String[] tags = new String[] {"TagA", "TagB", "TagC", "TagD", "TagE"};
        for (int i = 0; i < 100; i++) {
            //模拟10个订单流程，每个订单有10个顺序消息（每个订单有上面标签中的一个标签）
            //每个订单内的消息是顺序的，订单之间是没有相互影响的
            int orderId = i % 10;   //订单ID 0-9, 比如i=0、10、20...90分别表示订单0的10个消息

            Message msg = new Message("quickstart-ordered-topic",
                    tags[i % tags.length],
                    "KEY" + i,
                    ("Hello RocketMQ Ordered Msg " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));

            /**
             * MessageQueueSelector
             * 功能：通过给定的实现配合Topic现有队列和orderId，决定消息应该写入到哪些队列
             * !!! 说白了就是将同一个事务的多个操作按顺序入队列。
             */
            SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    //arg即传入的orderId, 0 1 2...9 0 1 ..
                    Integer id = (Integer) arg;
                    int index = id % mqs.size();
                    System.out.println("mqs size: " + mqs.size());
                    //同一个订单的消息顺序加入同一个队列
                    return mqs.get(index);
                }
            }, orderId);
            System.out.printf("%s%n", sendResult);
        }

        producer.shutdown();
    }
}
