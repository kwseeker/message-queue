package top.kwseeker.mq.rocketmq.advance.transaction;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import top.kwseeker.mq.rocketmq.Config;

import java.io.UnsupportedEncodingException;

public class TransactionProducer {

    public static void main(String[] args) throws MQClientException, InterruptedException {

        TransactionMQProducer producer = new TransactionMQProducer("transaction-producer");
        producer.setNamesrvAddr(Config.NAMESRV_ADDR);
        //事务执行的listener，用户实现接口提供本地事务执行代码实现（消息发送成功ＭＱ服务器返回确认后执行），以及回查本地事务处理结果的逻辑（MQ服务器一直收不到本地事务执行状态后执行）
        TransactionListener transactionListener = new TransactionListenerImpl();
        producer.setTransactionListener(transactionListener);
        producer.start();

        //模拟发送5条消息（５个分布式事务）
        for (int i = 1; i < 6 ; i++) {
            try {
                Message msg = new Message("transaction-topic", null, "msg-"+i,
                        ("给xxx加积分100(业务中这里可能是对象序列化字符串或json)" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                TransactionSendResult result = producer.sendMessageInTransaction(msg, null);
                System.out.println(result);
                Thread.sleep(10);
            } catch (MQClientException | UnsupportedEncodingException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        Thread.sleep(Integer.MAX_VALUE);
        producer.shutdown();
    }
}
