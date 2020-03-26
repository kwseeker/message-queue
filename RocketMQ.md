# RocketMQ

## RocketMQ架构

Producer、NamesrvCluster、BrokerCluster、Cousumer。

![](http://rocketmq.apache.org/assets/images/rmq-basic-arc.png)

+ **Producer Group**

+ **Consumer Group**

  一个 Consumer Group 下包含多个 Consumer 实例,可以是多台机器,也可以是多个进程,或者是一个进程的多个 Consumer 对象。一个 Consumer Group 下的多个 Consumer 以均摊方式消费消息,如果设置为广播方式,那么这个 Consumer Group 下的每个实例都消费全量数据。

+ **Name Server**  
  专为 RocketMQ 设计的轻量级名称服务,代码小亍 1000 行,具有简单、可集群横吐扩展、无状态等特点。将要支持的主备自劢切换功能会强依赖 Name Server。

+ **Topic和Broker、Queue、消费者的关系**

  ！队列又分为队列。

  一个Topic的多个消息可能在不同的Broker中不同的Queue上存储。
  一个消费者只能消费一种Topic。
  ![](imgs/Topic与Broker和Queue的关系.png)

+ **Broker主从的作用**

  多Broker主节点是为了数据分片存储与高可用；  
  Broker主从节点提升消息可靠性（防止机器故障、磁盘损坏），个人认为Broker主从应该称为主备，因为ＭＱ消息读取后就不应该再存在于队列中了，
  如果要做主从读写分离，读完之后需要同步更新其他所有队列，和关系型数据库读写分离不同，这里没有意义。

## 消息发送、存储与消费流程

### 消息结构

+ Producer端

  Topic + Body + Tags + Keys + Flag + DelayTimeLevel + WaitStoreMsgOK 

+ Consumer端

  由于 Broker 会为 Message增加数据结构,所以消息到达 Consumer 后,会在 Message 基础上增加多个字段, Consumer 看到的是
MessageExt 这个数据结构,MessageExt 继承于 Message。

### 消息发送

### 消息存储与数据同步

### 消息消费

#### 消费算法

+ 平均分配算法

+ 环形平均算法

+ 机房临近法

#### 消费详细流程

1）新建`DefaultMQPushConsumer`对象，设置订阅的Topic及Tags和消费位移模式；  
2）注册消息监听器，有两种监听器接口`MessageListenerOrderly`和`MessageListenerConcurrently`;  
3）这个只是设置一下`DefaultMQPushConsumerImpl`的`messageListenerInner`成员变量，真正的注册在`DefaultMQPushConsumer$start()`方法中。
通过调用`MQClientInstance$registerConsumer()`方法将监听器注册到 `consumerTable (ConcurrentMap<String, MQConsumerInner>)`，典型的观察者模式。  
```
public boolean registerConsumer(String group, MQConsumerInner consumer) {
        if (null != group && null != consumer) {
            MQConsumerInner prev = (MQConsumerInner)this.consumerTable.putIfAbsent(group, consumer);
            ...
        } else {
            return false;
        }
    }
```
4）启动监听线程,`RebalanceService$run()`,一直阻塞等待＋轮询等待消息。  
```
public void run() {
    this.log.info(this.getServiceName() + " service started");
    while(!this.isStopped()) {
        this.waitForRunning(waitInterval);
        //参考第５）步分析
        this.mqClientFactory.doRebalance();
    }
    this.log.info(this.getServiceName() + " service end");
}
```
5）当有消息到来，执行doRebalance()方法；  
第一步做Round-Robin选择消费者组中的一个消费者（API `MQConsumerInner`）进行处理消息；有两种实现`DefaultMQPullConsumerImpl`、`DefaultMQPushConsumerImpl`。  
5.1) DefaultMQPushConsumerImpl消费消息
```java
DefaultMQPushConsumerImpl$rebalanceImpl.doRebalance(this.isConsumeOrderly());
  RebalanceImpl$rebalanceByTopic(topic, isOrder);
    //有两种消费模式模式
    //1 广播消费

    //2 集群消费

```

#### pull 和 push

push方式里，consumer把轮询过程封装了，并注册MessageListener监听器，取到消息后，唤醒MessageListener的consumeMessage()来消费，对用户而言，感觉消息是被推送过来的。

pull方式里，取消息的过程需要用户自己写，首先通过打算消费的Topic拿到MessageQueue的集合，遍历MessageQueue集合，然后针对每个MessageQueue批量取消息，一次取完后，记录该队列下一次要取的开始offset，直到取完了，再换另一个MessageQueue。

#### 集群消费和广播消费

广播消费：一条消息被多个 Consumer 消费,即使这些 Consumer 属于同一个 Consumer Group,消息也会被 Consumer Group 中的每个 Consumer 都消费一次。

### 事务消息

通过RocketMQ消息队列实现分布式事务,4.3.0版本之后已经提供了默认支持；

比如下单场景，扣款在AccountService,加积分在MemberService中；
要确保扣款和加积分同时成功或失败。

对应伪代码
```
transaction {
  扣款();
  boolean success = 发送MQ();
  if(success) {
    commit();
  } else {
    rollback();
  }
}
```

![](imgs/RocketMQ实现分布式事务.png)

１）扣款服务发送消息（消息内容是为某用户加多少积分）；  
２）MQ服务器接收到消息存储下来，返回一个确认消息，告诉扣款服务消息发送成功；  
３）扣款服务接收到确认消息后执行扣款本地事务；   
４）扣款成功则再向MQ服务器发送commit请求消息；失败则发送rollback请求消息；ＭＱ服务器接收到commit或rollback,分别执行设置之前保存的消息对MQ订阅方可见或丢弃之前保存的消息；  
５）如果一段时间MQ没有接收到第４步的事务状态（commit/rollback）,则向MQ发送方发送请求（？怎么实现）要求发送方回查事务状态；  
６）扣款服务查询本地事务状态;  
７）根据事务的状态发送commit/rollback请求消息；ＭＱ服务器接收到commit或rollback,分别执行设置之前保存的消息对MQ订阅方可见或丢弃之前保存的消息；  

经过前面７步，分布式事务失败可能性已经很小了;  
但还是可能有问题：可能扣款成功但是加积分失败,可能由于是MQ订阅方消费消息失败进入死信队列或者更新数据库的时候失败要记录失败日志；由人工干预（不可能做到万物一失，就像https安全一样）。

### 消息使用规范

1） 一个应用尽可能用一个 Topic,消息子类型用 tags 来标识,tags 可以由应用自由设置。只有发送消息设置了
tags,消费方在订阅消息时,才可以利用 tags 在 broker 做消息过滤。

2） 每个消息在业务局面的唯一标识码,要设置到 keys 字段,方便将来定位消息丢失问题。服务器会为每个消息创建索引(哈希索引)
,应用可以通过 topic,key 来查询返条消息内容,以及消息被谁消费。由于是哈希索引,请务必保证 key 尽可能唯一,返样可以避免潜在的哈希冲突。

3） 消息収送成功戒者失败,要打印消息日志,务必要打印 sendresult 和 key 字段。

4） 消息发送成功后，需要根据返回的确认信息做后置处理。
  ```
  SEND_OK
  消息収送成功
  FLUSH_DISK_TIMEOUT
  消息収送成功,但是服务器刷盘超时,消息已经进入服务器队列,只有此时服务器宕机,消息才会丢失
  FLUSH_SLAVE_TIMEOUT
  消息収送成功,但是服务器同步到 Slave 时超时,消息已经进入服务器队列,只有此时服务器宕机,消息才会丢失
  SLAVE_NOT_AVAILABLE
  消息发送成功,但是此时 slave 不可用,消息已经进入服务器队列,只有此时服务器宕机,消息才会丢失
  ```

5）对亍消息不可丢失应用,务必要有消息重发机制,例如如果消息发送失败,存储到数据库,能有定时程序尝试重发,或者人工触发重发。

### 消息刷盘

+ 同步刷盘

+ 异步刷盘

### 顺序消息

将具有先后顺序的消息放在同一个队列

### 消息过滤

实现原理：  
1）Broker 所在的机器会启动多个 FilterServer 过滤进程;  
2）Consumer 启动后,会向 FilterServer 上传一个过滤的 Java 类（里面定义过滤规则）  
3）Consumer 从 FilterServer 拉消息FilterServer 将请求转发给 Broker,FilterServer 从 Broker 收到消息后,按照
Consumer 上传的 Java 过滤程序做过滤,过滤完成后返回给 Consumer。

## 特点

### 零拷贝


## 常见问题

+ **消息发布订阅**

  producer.send() 与　consumer.subscribe()

+ **重复消费问题**

  RocketMQ 无法避免消息重复,所以如果业务对消费重复非常敏感,务必
要在业务局面去重,有以下几种去重方式

  1. 记录能代表消息唯一性的字段，消费前判断是否已经消费过  
  将消息的唯一键,可以是 msgId,也可以是消息内容中的唯一标识字段,例如订单 Id 等,消费前判断是否在
  Db 或 Tair(全局KV存储)中存在,如果不存在则插入,并消费,否则跳过。
  (实际过程要考虑原子性问题,判断是否存在可以尝试插入,如果报主键冲突,则插入失败,直接跳过)
  msgId 一定是全局唯一标识符,但是可能会存在同样的消息有两个不同 msgId 的情况(有多种原因)，这种情况建议最好使用消息内容中的唯一标识字段去重。

  2. 使用业务层面的状态机去重

+ **消息去重**



+ **消息发送失败**

  启用Producer send 内部重试功能。   
  1. 至多重试 3 次。
  2. 如果収送失败,则轮转到下一个 Broker。
  3. 返个方法的总耗时时间丌超过 sendMsgTimeout 设置的值,默讣 10s。

+ **队列已满处理策略，有大量消息堆积怎么处理**

  RocketMQ队列为无线长度队列，但是　Broker 只保存3天的消息,那么这个 Buffer 虽然长度无限,但是3天前的数据会被从队尾删除。

+ **如何提高消费效率**

  1. 同一个 ConsumerGroup 下,通过增加 Consumer 实例数量来提高并行度,超过订阅队列数的 Consumer 实例无效。
    可以通过加机器,戒者在已有机器启劢多个迕程的方式。
  2. 提高单个 Consumer 的消费幵行线程,通过修改以下参数
    consumeThreadMin
    consumeThreadMax
  3. 跳过非重要消息
  4. 优化消费者性能
  
+ **如何实现延时消费**

  比如订单未支付，30分钟有效，超时取消订单。  
  使用`Message$setDelayTimeLevel()`方法; 消息发送后并不是直接发到创建订单的队列而是先发到一个延时队列，等待一段时间后才会将消息重延时队列发到创建订单的队列; 可以在到期时间到来后拦截消息做下用户是否付费检查，已经付费的话再将订单发给生成订单的队列。

+ **实现顺序性消息**


+ **消费幂等**


+ **如何确保消息可靠性传递，如何避免消息丢失**

+ **解决消息队列延时和过期失效问题**

## 其他概念

### Open Messaging

感觉相当于开发一个适配层，可以适配各种不同的消息中间件。
就像log4j和各种日志实现差不多。