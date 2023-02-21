package com.api.saga.amqp;

import java.io.Serializable;
import com.api.saga.dtos.ClienteDto;

public class ClienteTransfer implements Serializable {
    ClienteDto clienteDto;
    String action;
    String message;

    public ClienteTransfer() {
    }

    public ClienteTransfer(String message, String action) {
        this.message = message;
        this.action = action;
    }

    public ClienteTransfer(ClienteDto clienteDto, String action) {
        this.clienteDto = clienteDto;
        this.action = action;
    }

    public ClienteTransfer(ClienteDto clienteDto, String message, String action) {
        this.clienteDto = clienteDto;
        this.message = message;
        this.action = action;
    }

    public ClienteDto getClienteDto() { return this.clienteDto; }

    public void setClienteDto(ClienteDto clienteDto) { this.clienteDto = clienteDto; }

    public String getAction() { return this.action; }

    public void setAction(String action) { this.action = action; }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}