package com.api.saga.amqp;

import com.api.saga.dtos.UserDto;
import java.io.Serializable;

public class UserTransfer implements Serializable {
    UserDto userDto;
    String action;
    String message;

    public UserTransfer() {
    }

    public UserTransfer(UserDto userDto, String action) {
        this.userDto = userDto;
        this.action = action;
    }

    public UserDto getUserDto() {
        return this.userDto;
    }

    public void setUserDto(UserDto userDto) {
        this.userDto = userDto;
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