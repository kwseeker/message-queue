package top.kwseeker.rocketmq.example;

import com.alibaba.fastjson.TypeReference;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQLocalRequestCallback;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeTypeUtils;
import top.kwseeker.rocketmq.example.model.OrderPaidEvent;
import top.kwseeker.rocketmq.example.model.ProductWithPayload;
import top.kwseeker.rocketmq.example.model.User;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RocketMQTemplateTest {

    //命名规范遵从官方格式 rmq_sys_SYNC_BROKER_MEMBER_KdcNode00
    private static final String springBootTestTopic = "rmq_user_BOOT_TEST";

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送接口是spring重新封装的，可以传递 Object、Collection、Message (当前两种实现 GenericMessage、ErrorMessage) 等类型消息
     * Object Collection 类型消息其实会被 MessageBuilder 封装成 Message 类型消息
     * TODO 后面看源码时关注下序列化方式
     */
    @Test
    public void testSendDifferentTypeMessage() {
        //同步发送--------------------------------

        SendResult sendResult;

        //同步发送字符消息
        sendResult = rocketMQTemplate.syncSend(springBootTestTopic, "Hello, World!");
        System.out.printf("syncSend string message to topic %s, sendResult=%s %n", springBootTestTopic, sendResult);

        //同步发送对象消息
        sendResult = rocketMQTemplate.syncSend(springBootTestTopic, new User().setAge((byte) 18).setName("Kitty"));
        System.out.printf("syncSend object message to topic %s, sendResult=%s %n", springBootTestTopic, sendResult);

        //在消息上附带额外信息
        sendResult = rocketMQTemplate.syncSend(springBootTestTopic, MessageBuilder
                .withPayload(new User().setAge((byte) 21).setName("Lester"))
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                .build());
        System.out.printf("syncSend message with header to topic %s, sendResult=%s %n", springBootTestTopic, sendResult);

        //异步发送--------------------------------

        //异步发送对象消息
        //关于回调处理实现，可以本地维护一个环形队列，消息异步发送后将消息信息入对，收到消息正常发送回调就将消息从队列移除，反之失败的话，就重试
        //TODO 简单实现下回调处理
        rocketMQTemplate.asyncSend(springBootTestTopic, new OrderPaidEvent("T_001", new BigDecimal("88.00")), new SendCallback() {
            @Override
            public void onSuccess(SendResult var1) {
                System.out.printf("async onSuccess SendResult=%s %n", var1);
            }

            @Override
            public void onException(Throwable var1) {
                System.out.printf("async onException Throwable=%s %n", var1);
            }
        });
    }

    @Test
    public void testConvertAndSendMessage() {
        //发送指定TAG的消息
        rocketMQTemplate.convertAndSend(springBootTestTopic + ":tag0", "I'm from tag0");  // tag0 will not be consumer-selected
        System.out.printf("syncSend topic %s tag %s %n", springBootTestTopic, "tag0");
        rocketMQTemplate.convertAndSend(springBootTestTopic + ":tag1", "I'm from tag1");
        System.out.printf("syncSend topic %s tag %s %n", springBootTestTopic, "tag1");
    }

    @Test
    public void testSendAndReceiveMessage() {
        //同步发送消息并且返回一个String类型的结果。
        String replyString = rocketMQTemplate.sendAndReceive(springBootTestTopic, "request string", String.class);
        System.out.printf("send %s and receive %s %n", "request string", replyString);

        //同步发送消息并且返回一个Byte数组类型的结果。
        byte[] replyBytes = rocketMQTemplate.sendAndReceive(springBootTestTopic, MessageBuilder.withPayload("request byte[]").build(), byte[].class, 3000);
        System.out.printf("send %s and receive %s %n", "request byte[]", new String(replyBytes));

        //同步发送一个带hash参数的请求(排序消息)，并返回一个User类型的结果
        User requestUser = new User().setAge((byte) 9).setName("requestUserName");
        User requestUser2 = new User().setAge((byte) 9).setName("requestUserName");
        User requestUser3 = new User().setAge((byte) 9).setName("requestUserName");
        User requestUser4 = new User().setAge((byte) 9).setName("requestUserName");
        User replyUser = rocketMQTemplate.sendAndReceive(springBootTestTopic, requestUser, User.class, "order-id");
        User replyUser2 = rocketMQTemplate.sendAndReceive(springBootTestTopic, requestUser2, User.class, "order-id");
        User replyUser3 = rocketMQTemplate.sendAndReceive(springBootTestTopic, requestUser3, User.class, "order-id");
        User replyUser4 = rocketMQTemplate.sendAndReceive(springBootTestTopic, requestUser4, User.class, "order-id");
        System.out.printf("send %s and receive %s %n", requestUser, replyUser);
        //同步发送一个带延迟级别的消息(延迟消息)，并返回一个泛型结果
        ProductWithPayload<String> replyGenericObject = rocketMQTemplate.sendAndReceive(springBootTestTopic, "request generic",
                new TypeReference<ProductWithPayload<String>>() {
                }.getType(), 30000, 2);
        System.out.printf("send %s and receive %s %n", "request generic", replyGenericObject);

        //异步发送消息，返回String类型结果。
        rocketMQTemplate.sendAndReceive(springBootTestTopic, "request string", new RocketMQLocalRequestCallback<String>() {
            @Override public void onSuccess(String message) {
                System.out.printf("send %s and receive %s %n", "request string", message);
            }

            @Override public void onException(Throwable e) {
                e.printStackTrace();
            }
        });
        //异步发送消息，并返回一个User类型的结果。
        rocketMQTemplate.sendAndReceive(springBootTestTopic, new User().setAge((byte) 9).setName("requestUserName"),
                new RocketMQLocalRequestCallback<User>() {
                    @Override public void onSuccess(User message) {
                        System.out.printf("send user object and receive %s %n", message.toString());
                    }

                    @Override public void onException(Throwable e) {
                        e.printStackTrace();
                    }
                }, 5000);
    }

    @Test
    public void testSendBatchMessage() {
        //发送批量消息
        List<Message<String>> messages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Message<String> message = MessageBuilder.withPayload("Hello RocketMQ Batch Msg#" + i)
                    .setHeader(RocketMQHeaders.KEYS, "KEY_" + i)
                    .build();
            messages.add(message);
        }
        SendResult sr = rocketMQTemplate.syncSend(springBootTestTopic, messages, 60000);
        System.out.println("--- Batch messages send result :" + sr);
    }

    @Test
    public void testReceiveMessage(){

    }
}
