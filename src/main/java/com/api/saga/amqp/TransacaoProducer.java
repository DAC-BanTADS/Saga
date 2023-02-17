package com.api.saga.amqp;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.amqp.core.Queue;
import com.api.saga.dtos.TransacaoDto;

public class TransacaoProducer {
    @Autowired
    private RabbitTemplate template;

    @Autowired
    @Qualifier("transacao")
    private Queue queue;

    public void send(TransacaoTransfer transacaoTransfer) {
        this.template.convertAndSend(this.queue.getName(), transacaoTransfer);
    }

    public TransacaoTransfer sendAndReceive(TransacaoDto transacaoDto, String action) {
        TransacaoTransfer transacaoTransfer = new TransacaoTransfer(transacaoDto, action);
        return (TransacaoTransfer) this.template.convertSendAndReceive(this.queue.getName(), transacaoTransfer);
    }
}
