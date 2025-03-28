package com.ll.demo03.domain.task.dto;

import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class AckInfo {
    private Channel channel;
    private long deliveryTag;
}