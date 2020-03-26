package top.kwseeker.mq.rocketmq.quickstart;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * 消费者：单向发送消息，不会返回确认信息。
 * 不能确保消息可靠性投递，适用于日志收集等场景。
 */
public class OnewayProducer {

    public static void main(String[] args) throws Exception{

        DefaultMQProducer producer = new DefaultMQProducer(Config.PRODUCER_NAME);
        producer.setNamesrvAddr(Config.NAMESRV_ADDR);
        producer.start();

        for (int i = 0; i < 10; i++) {
            Message msg = new Message(Config.TOPIC_NAME /* Topic */,
                    "TagA" /* Tag */,
                    ("Hello RocketMQ Oneway Msg " + i).getBytes(RemotingHelper.DEFAULT_CHARSET) /* Message body */
            );
            //这种方式RocketMQ服务端不会返回确认信息
            producer.sendOneway(msg);
        }

        producer.shutdown();
    }
}