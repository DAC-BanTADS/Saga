package com.api.saga.controllers;

import com.api.saga.amqp.*;
import com.api.saga.dtos.ClienteDto;
import com.api.saga.dtos.ContaDto;
import com.api.saga.dtos.GerenteDto;
import com.api.saga.dtos.TransacaoDto;
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
    @Autowired
    private GerenteProducer gerenteProducer;
    @Autowired
    private TransacaoProducer transacaoProducer;

    @PostMapping("/cliente")
    public ResponseEntity<Object> saveCliente(@RequestBody ClienteDto clienteDto) {
        try {
            ClienteTransfer clienteTransfer = clienteProducer.sendAndReceive(clienteDto, "save-cliente");

            if (clienteTransfer.getAction().equals("success-cliente")) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Cliente criado com sucesso!");
            } else if (clienteTransfer.getAction().equals("failed-cliente")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(clienteTransfer.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
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
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(contaTransfer.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao criar a conta");
    }

    @PostMapping("/gerente")
    public ResponseEntity<Object> saveGerente(@RequestBody GerenteDto gerenteDto) {
        try {
            GerenteTransfer gerenteTransfer = gerenteProducer.sendAndReceive(gerenteDto, "save-gerente");

            if (gerenteTransfer.getAction().equals("success-gerente")) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Gerente criado com sucesso!");
            } else if (gerenteTransfer.getAction().equals("failed-gerente")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(gerenteTransfer.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao criar o gerente");
    }

    @PostMapping("/transacao")
    public ResponseEntity<Object> saveTransacao(@RequestBody TransacaoDto transacaoDto) {
        try {
            TransacaoTransfer transacaoTransfer = transacaoProducer.sendAndReceive(transacaoDto, "save-transacao");

            if (transacaoTransfer.getAction().equals("success-transacao")) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Transacao criada com sucesso!");
            } else if (transacaoTransfer.getAction().equals("failed-transacao")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(transacaoTransfer.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao criar a transacao");
    }
}
