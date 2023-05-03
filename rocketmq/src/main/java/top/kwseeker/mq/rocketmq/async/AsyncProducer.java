package top.kwseeker.mq.rocketmq.async;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import top.kwseeker.mq.rocketmq.Config;

import java.util.concurrent.CountDownLatch;

/**
 * 生产者：异步发送消息
 */
public class AsyncProducer {

    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(Config.PRODUCER_NAME);
        producer.setNamesrvAddr(Config.NAMESRV_ADDR);
        //消息发送失败重试
        producer.setRetryTimesWhenSendAsyncFailed(3);
        producer.start();

        int msgCount = 100;
        CountDownLatch countDownLatch = new CountDownLatch(msgCount);
        for (int i = 0; i < msgCount; i++) {
            final int index = i;
            //Create a message instance, specifying topic, tag and message body.
            Message msg = new Message(Config.TOPIC_NAME,
                    "TagA",             //TODO
                    "OrderID188",       //TODO
                    ("Hello world " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));

            producer.send(msg, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.printf("%-10d OK %s %n", index,
                            sendResult.getMsgId());
                    countDownLatch.countDown();
                }
                @Override
                public void onException(Throwable e) {
                    System.out.printf("%-10d Exception %s %n", index, e);
                    e.printStackTrace();
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
        producer.shutdown();
    }
}
