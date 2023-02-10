package com.api.saga.amqp;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.amqp.core.Queue;
import com.api.saga.dtos.ClienteDto;

public class ClienteProducer {
    @Autowired
    private RabbitTemplate template;

    @Autowired
    @Qualifier("cliente")
    private Queue queue;

    public void send(ClienteTransfer clienteTransfer) {
        this.template.convertAndSend(this.queue.getName(), clienteTransfer);
    }

    public ClienteTransfer sendAndReceive(ClienteDto clienteDto, String action) {
        ClienteTransfer clienteTransfer = new ClienteTransfer(clienteDto, action);
        return (ClienteTransfer) this.template.convertSendAndReceive(this.queue.getName(), clienteTransfer);
    }
}
