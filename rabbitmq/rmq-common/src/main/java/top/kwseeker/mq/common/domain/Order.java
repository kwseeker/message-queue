package top.kwseeker.mq.common.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Order implements Serializable {

    //订单id
    private Integer id;
    //用户id
    private Integer uid;
    //商品id
    private Integer goodsId;
    //消息id
    private Integer messageId;
}
