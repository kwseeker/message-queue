package top.kwseeker.mq.rmqpublisher.service;

import top.kwseeker.mq.common.domain.Order;

public interface OrderService {

    void createOrder(Order order);
}
