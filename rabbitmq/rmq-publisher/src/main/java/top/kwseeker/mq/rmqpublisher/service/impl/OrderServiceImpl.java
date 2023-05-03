package top.kwseeker.mq.rmqpublisher.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import top.kwseeker.mq.common.constants.Constants;
import top.kwseeker.mq.common.domain.MessageLog;
import top.kwseeker.mq.common.domain.Order;
import top.kwseeker.mq.common.mapper.OrderMapper;
import top.kwseeker.mq.rmqpublisher.service.OrderService;

import java.util.Date;

public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private MessageLogMapper messageLogMapper;

    @Value("${spring.rabbitmq.retry-delay-time}")
    private int retryDelayTime;

    @Override
    public void createOrder(Order order) {
        //1) 将订单插入MySQL订单记录表
        orderMapper.insert(order);
        //2) 记录消息日志到MySQL消息日志表
        MessageLog messageLog = new MessageLog()
            .setMessageId(order.getMessageId())
            .setMessageContent(JSONObject.toJSONString(order))
            .setStatus(Constants.ORDER_SENDING)
            .setCreateTime(new Date())
            .setNextRetryTime(new Date(new Date().getTime() + retryDelayTime*1000))
            .setRetryCount(0);
        messageLogMapper.insert(messageLog);
        //3) 发送消息

    }
}
