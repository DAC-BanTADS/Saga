package com.api.saga.amqp;

import com.api.saga.dtos.TransacaoDto;
import java.io.Serializable;

public class TransacaoTransfer implements Serializable {
    TransacaoDto transacaoDto;
    String action;
    String message;

    public TransacaoTransfer() {
    }

    public TransacaoTransfer(TransacaoDto transacaoDto, String action) {
        this.transacaoDto = transacaoDto;
        this.action = action;
    }

    public TransacaoDto getTransacaoDto() {
        return this.transacaoDto;
    }

    public void setTransacaoDto(TransacaoDto transacaoDto) {
        this.transacaoDto = transacaoDto;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}