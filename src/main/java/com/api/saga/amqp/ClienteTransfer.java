package com.api.saga.amqp;

import java.io.Serializable;
import com.api.saga.models.ClienteDto;

public class ClienteTransfer implements Serializable {
    ClienteDto clienteDto;
    String action;

    public ClienteTransfer() {
    }

    public ClienteTransfer(ClienteDto clienteDto, String action) {
        this.clienteDto = clienteDto;
        this.action = action;
    }

    public ClienteDto getCliente() {
        return clienteDto;
    }

    public void setCliente(ClienteDto clienteDto) {
        this.clienteDto = clienteDto;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}