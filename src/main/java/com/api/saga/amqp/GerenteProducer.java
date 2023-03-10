package com.api.saga.amqp;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.amqp.core.Queue;
import com.api.saga.dtos.GerenteDto;

public class GerenteProducer {
    @Autowired
    private RabbitTemplate template;

    @Autowired
    @Qualifier("gerente")
    private Queue queue;

    public void send(GerenteTransfer gerenteTransfer) {
        this.template.convertAndSend(this.queue.getName(), gerenteTransfer);
    }

    public GerenteTransfer sendAndReceive(GerenteDto gerenteDto, String action) {
        GerenteTransfer gerenteTransfer = new GerenteTransfer(gerenteDto, action);
        return (GerenteTransfer) this.template.convertSendAndReceive(this.queue.getName(), gerenteTransfer);
    }

    public GerenteTransfer sendAndReceive(String action) {
        GerenteTransfer gerenteTransfer = new GerenteTransfer(action);
        return (GerenteTransfer) this.template.convertSendAndReceive(this.queue.getName(), gerenteTransfer);
    }

    public GerenteTransfer sendAndReceive(String message, String action) {
        GerenteTransfer gerenteTransfer = new GerenteTransfer(message, action);
        return (GerenteTransfer) this.template.convertSendAndReceive(this.queue.getName(), gerenteTransfer);
    }

    public GerenteTransfer sendAndReceive(GerenteDto gerenteDto, String message, String action) {
        GerenteTransfer gerenteTransfer = new GerenteTransfer(gerenteDto, message, action);
        return (GerenteTransfer) this.template.convertSendAndReceive(this.queue.getName(), gerenteTransfer);
    }
}
