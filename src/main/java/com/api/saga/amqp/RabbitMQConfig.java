package com.api.saga.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Bean
    @Qualifier("cliente")
    public Queue clientQueue() {
        return new Queue("cliente");
    }

    @Bean
    @Qualifier("conta")
    public Queue contaQueue() {
        return new Queue("conta");
    }

    @Bean
    @Qualifier("gerente")
    public Queue gerenteQueue() {
        return new Queue("gerente");
    }

    @Bean
    @Qualifier("transacao")
    public Queue transacaoQueue() {
        return new Queue("transacao");
    }

    @Bean
    public ClienteProducer clienteProducer() {
        return new ClienteProducer();
    }

    @Bean
    public ContaProducer contaProducer() {
        return new ContaProducer();
    }

    @Bean
    public GerenteProducer gerenteProducer() {
        return new GerenteProducer();
    }

    @Bean
    public TransacaoProducer transacaoProducer() {
        return new TransacaoProducer();
    }

    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        classMapper.setTrustedPackages("*");

        idClassMapping.put("com.api.cliente.amqp.ClienteTransfer", ClienteTransfer.class);
        idClassMapping.put("com.api.conta.amqp.ContaTransfer", ContaTransfer.class);
        idClassMapping.put("com.api.gerente.amqp.GerenteTransfer", GerenteTransfer.class);
        idClassMapping.put("com.api.transacao.amqp.TransacaoTransfer", TransacaoTransfer.class);

        classMapper.setIdClassMapping(idClassMapping);

        return classMapper;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper());

        return converter;
    }
}

