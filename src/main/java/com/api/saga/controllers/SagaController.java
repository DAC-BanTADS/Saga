package com.api.saga.controllers;

import com.api.saga.amqp.ClienteProducer;
import com.api.saga.amqp.ClienteTransfer;
import com.api.saga.models.ClienteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class SagaController {
    @Autowired
    private ClienteProducer clienteProducer;

    @PostMapping(value = "/cliente", produces = "application/json")
    public ResponseEntity<Object> saveCliente(@RequestBody ClienteDto clienteDto) {
        try {
            ClienteTransfer clienteTransfer = clienteProducer.sendAndReceive(clienteDto, "save-cliente");

            if (clienteTransfer.getAction().equals("success-cliente")) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Cliente criado com sucesso!");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao criar o cliente");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao criar o cliente");
    }
}
