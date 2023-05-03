package top.kwseeker.mq.common.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import top.kwseeker.mq.common.domain.Order;

@Mapper
public interface OrderMapper {

    //long countByExample(OrderExample example);
    //
    //int deleteByExample(OrderExample example);
    //
    //int deleteByPrimaryKey(Integer id);

    @Insert("insert into t_order (uid, goodsId, messageId) values (#{uid}, #{goodsId}, #{messageId})")
    @Options(useGeneratedKeys = true, keyColumn = "id")
    void insert(Order record);

    //int insertSelective(Order record);
    //
    //List<Order> selectByExample(OrderExample example);
    //
    //Order selectByPrimaryKey(Integer id);
    //
    //int updateByExampleSelective(@Param("record") Order record, @Param("example") OrderExample example);
    //
    //int updateByExample(@Param("record") Order record, @Param("example") OrderExample example);
    //
    //int updateByPrimaryKeySelective(Order record);
    //
    //int updateByPrimaryKey(Order record);
}
