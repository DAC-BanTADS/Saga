package com.api.saga.amqp;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.amqp.core.Queue;
import com.api.saga.dtos.UserDto;

public class UserProducer {
    @Autowired
    private RabbitTemplate template;

    @Autowired
    @Qualifier("user")
    private Queue queue;

    public void send(UserTransfer userTransfer) {
        this.template.convertAndSend(this.queue.getName(), userTransfer);
    }

    public UserTransfer sendAndReceive(UserDto userDto, String action) {
        UserTransfer userTransfer = new UserTransfer(userDto, action);
        return (UserTransfer) this.template.convertSendAndReceive(this.queue.getName(), userTransfer);
    }
}
