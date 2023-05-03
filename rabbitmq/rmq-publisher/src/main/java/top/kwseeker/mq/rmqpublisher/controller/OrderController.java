package top.kwseeker.mq.rmqpublisher.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.kwseeker.mq.common.domain.Order;
import top.kwseeker.mq.rmqpublisher.service.OrderService;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class OrderController {

    private AtomicInteger idGenerator = new AtomicInteger(1);

    @Autowired
    private OrderService orderService;

    /**
     * 以下单为例，模拟消息可靠性投递
     * 订单服务　作为rmq-publisher
     * 假如下单之后还有一些后续操作，如：1) 加积分　2)发优惠券　3) 货仓减储　...   这些后续服务作为rmq-consumer
     *
     * 可靠性消息投递方案１：　消息落库，对消息状态进行打标
     *
     * 第1步：将订单入库，创建一条MSG(状态为0) 入MSG DB库
     * 第2步：将消息发出去
     * 第3步：监听消息应答(来自Broker)
     * 第4步：修改消息的状态为1(成功)
     * 第5步：分布式定时任务抓取状态为0的消息
     * 第6步：将状态为0的消息重发
     * 第7步：如果尝试了3次(可按实际情况修改)以上则将状态置为2(消息投递失败状态)
     *
     * 实现相对简单但是先后写两次消息记录数据库，性能可能不太好
     */
    @RequestMapping("/order")
    public String order() {
        Order order = new Order();
        int id = idGenerator.getAndIncrement();
        order.setId(id).setGoodsId(id).setUid(id).setMessageId(id);

        orderService.createOrder(order);

        return "success";
    }

    /**
     * 可靠性消息投递方案2：　消息延迟投递，做二次确认，回调检查
     */

}
