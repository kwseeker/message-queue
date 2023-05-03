package top.kwseeker.mq.common.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class MessageLog {

    //消息id
    private Integer messageId;
    //消息内容
    private String messageContent;
    //重试计数
    private Integer retryCount;
    //消息状态
    private Integer status;
    //
    private Date nextRetryTime;
    //
    private Date createTime;
    //
    private Date updateTime;

}
