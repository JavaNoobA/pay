package com.imooc.pay.service.impl;

import com.imooc.pay.dao.PayInfoMapper;
import com.imooc.pay.pojo.PayInfo;
import com.imooc.pay.service.IPayService;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author pengfei.zhao
 * @date 2020/11/1 11:48
 */
@Slf4j
@Service
public class PayService implements IPayService {

    @Autowired
    private BestPayService bestPayService;

    @Resource
    private PayInfoMapper payInfoMapper;

    @Override
    public PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum) {
        // 订单写入数据库
        PayInfo payInfo = new PayInfo(Long.parseLong(orderId),
                PayPlatformEnum.getByBestPayTypeEnum(bestPayTypeEnum).getCode(),
                OrderStatusEnum.NOTPAY.name(),
                amount);
        payInfoMapper.insertSelective(payInfo);

        PayRequest payRequest = new PayRequest();
        payRequest.setOrderName("4559066-最好的支付sdk");
        payRequest.setOrderId(orderId);
        payRequest.setOrderAmount(amount.doubleValue());
        payRequest.setPayTypeEnum(bestPayTypeEnum);

        PayResponse response = bestPayService.pay(payRequest);
        log.info("支付 response={}", response);

        return response;
    }

    @Override
    public String asyncNotify(String notifyData) {
        // 1.签名检验


        final PayResponse notifyResp = bestPayService.asyncNotify(notifyData);
        log.info("异步通知 response={}", notifyResp);

        PayInfo payInfo = payInfoMapper.selectByOrderNo(Long.parseLong(notifyResp.getOrderId()));

        // 正常情况下 支付信息不会为 null, 需要告警钉钉、短信等
        if (payInfo == null) {
            throw new RuntimeException("支付信息为 null");
        }

        // 2.金额检验(从数据库里查订单)
        if (payInfo.getPayAmount().compareTo(BigDecimal.valueOf(notifyResp.getOrderAmount())) != 0) {
            throw new RuntimeException("支付金额和数据库订单金额不一致");
        }

        // 3.修改订单支付状态
        if (!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())) {
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            payInfo.setPlatformNumber(notifyResp.getOutTradeNo());
            payInfoMapper.updateByPrimaryKeySelective(payInfo);
        }

        // TODO pay通过mq发支付消息 mall接收

        // 4.告诉微信/支付宝不要通知了
        if (notifyResp.getPayPlatformEnum() == BestPayPlatformEnum.WX) {
            return "<xml>\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
        } else if (notifyResp.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY) {
            return "success";
        }
        throw new RuntimeException("异步通知结果出错");
    }

    @Override
    public PayInfo queryByOrderId(String orderId) {
        return payInfoMapper.selectByOrderNo(Long.parseLong(orderId));
    }
}
