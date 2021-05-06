package com.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
@AllArgsConstructor
public class KafkaOrderDto implements Serializable {

    private Schema schema;
    private Payload payload;

}
