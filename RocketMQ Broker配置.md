# RocketMQ Broker配置

```properties
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#所属集群名字
brokerClusterName=rocketmq-cluster
#broker名字，注意此处不同的配置文件填写的不一样
brokerName=broker-a
#0 表示 Master，>0 表示 Slave
brokerId=0
#nameServer地址，分号分割
namesrvAddr=rocketmq1:9876;rocketmq2:9876
#在发送消息时，自动创建服务器不存在的topic，默认创建的队列数
defaultTopicQueueNums=4
#是否允许 Broker 自动创建Topic，建议线下开启，线上关闭
autoCreateTopicEnable=true
#是否允许 Broker 自动创建订阅组，建议线下开启，线上关闭
autoCreateSubscriptionGroup=true
#Broker 对外服务的监听端口
listenPort=10911
#删除文件时间点，默认凌晨 4点
deleteWhen=04
#文件保留时间，默认 48 小时
fileReservedTime=120
#commitLog每个文件的大小默认1G
mapedFileSizeCommitLog=1073741824
#ConsumeQueue每个文件默认存30W条，根据业务情况调整
mapedFileSizeConsumeQueue=300000
#强制删除文件间隔时间（单位毫秒）
destroyMapedFileIntervalForcibly=120000
#重试删除文件间隔
redeleteHangedFileInterval=120000
#检测物理文件磁盘空间
diskMaxUsedSpaceRatio=88
#存储路径
storePathRootDir=/usr/local/soft/rocketmq/data/store
#commitLog 存储路径
storePathCommitLog=/usr/local/soft/rocketmq/data/store/commitlog
#限制的消息大小
maxMessageSize=65536
#一次刷盘至少需要脏页的数量，针对commitlog文件
flushCommitLogLeastPages=4
#一次刷盘至少需要脏页的数量,默认2页,针对 Consume文件
flushConsumeQueueLeastPages=2
#commitlog两次刷盘的最大间隔,如果超过该间隔,将fushCommitLogLeastPages要求直接执行刷盘操作
flushCommitLogThoroughInterval=10000
#Consume两次刷盘的最大间隔,如果超过该间隔,将忽略
flushConsumeQueueThoroughInterval=60000
#是否开启回查功能
checkTransactionMessageEnable=false
#服务端处理消息发送线程池数量
sendMessageThreadPoolNums=128
#服务端处理消息拉取线程池线程数量 默认为16加上当前操作系统CPU核数的两倍
pullMessageThreadPoolNums=128
#broker角色,分为 ASYNC_MASTER SYNC_MASTER, SLAVE
brokerRole=SYNC_MASTER
#刷盘方式,默认为 ASYNC_FLUSH(异步刷盘),可选值SYNC_FLUSH(同步刷盘)
flushDiskType=ASYNC_FLUSHll
```