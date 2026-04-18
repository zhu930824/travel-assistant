package cn.sdh.travel.mapper;

import cn.sdh.travel.entity.domain.PaymentOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 支付订单Mapper
 */
@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {

    /**
     * 根据订单号查询
     */
    @Select("SELECT * FROM payment_order WHERE order_no = #{orderNo}")
    PaymentOrder findByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据用户ID查询订单列表
     */
    @Select("SELECT * FROM payment_order WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<PaymentOrder> findByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 查询过期的待支付订单
     */
    @Select("SELECT * FROM payment_order WHERE status = 0 AND expire_time < NOW()")
    List<PaymentOrder> findExpiredOrders();
}
