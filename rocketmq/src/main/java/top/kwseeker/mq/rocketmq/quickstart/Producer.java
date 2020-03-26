package top.kwseeker.mq.rocketmq.quickstart;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * 生产者：同步发送消息
 */
public class Producer {

    public static void main(String[] args) throws MQClientException {

        //同一个生产者组其实是使用的同一个生产者实例
        //roducer Group 作用如下:
        //1. 标识一类 Producer
        //2. 可以通过运维工具查询返个収送消息应用下有多个 Producer 实例
        //3. 収送分布式事务消息时,如果 Producer 中途意外宕机,Broker 会主动回调 Producer Group 内的任意一台机器来确认事务状态。
        DefaultMQProducer producer = new DefaultMQProducer(Config.PRODUCER_NAME);
        //设置注册中心
        producer.setNamesrvAddr(Config.NAMESRV_ADDR);
        //注册Producer、路由信息等等
        producer.start();

        try {
            //创建消息实体，指定topic
            //TAGS: 为消息设置Tag标签，用于实现消息过滤; 可以通过多次调用setTags()指定多个标签
            Message msg = new Message(Config.TOPIC_NAME, "TagA", "Hello RocketMQ !".getBytes(RemotingHelper.DEFAULT_CHARSET));
            SendResult sendResult = producer.send(msg);
            System.out.printf("send result: %s%n", sendResult);
        } catch (Exception e) {
            e.printStackTrace();
        }

        producer.shutdown();
    }
}
