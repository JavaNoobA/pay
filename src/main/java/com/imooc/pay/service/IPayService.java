package com.imooc.pay.service;

import com.imooc.pay.pojo.PayInfo;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;

import java.math.BigDecimal;

/**
 * @author pengfei.zhao
 * @date 2020/11/1 11:47
 */
public interface IPayService {

    /**
     * 创建/发起支付
     * @param orderId 订单号
     * @param amount 支付金额
     * @return 支付结果
     */
    PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum);

    /**
     * 异步支付通知结果
     * @param notifyData 回调数据
     * @return 支付结果
     */
    String asyncNotify(String notifyData);

    /**
     * 根据订单号查询支付信息
     * @param orderId 订单号
     * @return 支付信息
     */
    PayInfo queryByOrderId(String orderId);
}
