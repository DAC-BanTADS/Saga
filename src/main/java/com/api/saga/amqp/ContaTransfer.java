package com.api.saga.amqp;

import java.io.Serializable;
import com.api.saga.dtos.ContaDto;

public class ContaTransfer implements Serializable {
    ContaDto contaDto;
    String action;
    String message;

    public ContaTransfer() {
    }

    public ContaTransfer(ContaDto contaDto, String action) {
        this.contaDto = contaDto;
        this.action = action;
    }

    public ContaDto getContaDto() { return this.contaDto; }

    public void setContaDto(ContaDto contaDto) { this.contaDto = contaDto; }

    public String getAction() { return this.action; }

    public void setAction(String action) { this.action = action; }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}