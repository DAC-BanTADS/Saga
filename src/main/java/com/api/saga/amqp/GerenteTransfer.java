package com.api.saga.amqp;

import com.api.saga.dtos.GerenteDto;
import java.io.Serializable;

public class GerenteTransfer implements Serializable {
    GerenteDto gerenteDto;
    String action;

    public GerenteTransfer() {
    }

    public GerenteTransfer(GerenteDto gerenteDto, String action) {
        this.gerenteDto = gerenteDto;
        this.action = action;
    }

    public GerenteDto getGerenteDto() {
        return this.gerenteDto;
    }

    public void setGerenteDto(GerenteDto gerenteDto) {
        this.gerenteDto = gerenteDto;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}