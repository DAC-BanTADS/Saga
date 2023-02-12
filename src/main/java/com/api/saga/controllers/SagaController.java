package com.api.saga.controllers;

import com.api.saga.amqp.ClienteProducer;
import com.api.saga.amqp.ClienteTransfer;
import com.api.saga.amqp.ContaProducer;
import com.api.saga.amqp.ContaTransfer;
import com.api.saga.dtos.ClienteDto;
import com.api.saga.dtos.ContaDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class SagaController {
    @Autowired
    private ClienteProducer clienteProducer;
    @Autowired
    private ContaProducer contaProducer;

    @PostMapping("/cliente")
    public ResponseEntity<Object> saveCliente(@RequestBody ClienteDto clienteDto) {
        try {
            ClienteTransfer clienteTransfer = clienteProducer.sendAndReceive(clienteDto, "save-cliente");

            if (clienteTransfer.getAction().equals("success-cliente")) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Cliente criado com sucesso!");
            } else if (clienteTransfer.getAction().equals("failed-cliente")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Houve um erro na criação do cliente.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getCause().getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao criar o cliente");
    }

    @PostMapping("/conta")
    public ResponseEntity<Object> saveConta(@RequestBody ContaDto contaDto) {
        try {
            ContaTransfer contaTransfer = contaProducer.sendAndReceive(contaDto, "save-conta");

            if (contaTransfer.getAction().equals("success-conta")) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Conta criada com sucesso!");
            } else if (contaTransfer.getAction().equals("failed-conta")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Houve um erro na criação da conta.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getCause().getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao criar a conta");
    }
}
