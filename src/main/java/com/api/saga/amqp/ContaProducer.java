package com.api.saga.amqp;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.amqp.core.Queue;
import com.api.saga.dtos.ContaDto;

public class ContaProducer {
    @Autowired
    private RabbitTemplate template;

    @Autowired
    @Qualifier("conta")
    private Queue queue;

    public void send(ContaTransfer contaTransfer) {
        this.template.convertAndSend(this.queue.getName(), contaTransfer);
    }

    public ContaTransfer sendAndReceive(ContaDto contaDto, String action) {
        ContaTransfer contaTransfer = new ContaTransfer(contaDto, action);
        return (ContaTransfer) this.template.convertSendAndReceive(this.queue.getName(), contaTransfer);
    }
}
