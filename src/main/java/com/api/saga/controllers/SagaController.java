package com.api.saga.controllers;

import com.api.saga.amqp.*;
import com.api.saga.dtos.*;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

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
    @Autowired
    private UserProducer userProducer;

    @PostMapping("/cliente")
    public ResponseEntity<Object> saveCliente(@RequestBody ClienteDto clienteDto) {
        try {
            GerenteTransfer gerenteTransfer = gerenteProducer.sendAndReceive("min-gerente");

            if (gerenteTransfer.getAction().equals("success-gerente")) {

                ClienteTransfer clienteTransfer = clienteProducer.sendAndReceive(clienteDto, "save-cliente");

                if (clienteTransfer.getAction().equals("success-cliente")) {

                    ContaDto contaDto = new ContaDto();

                    contaDto.setIdCliente(UUID.fromString(clienteTransfer.getMessage()));
                    contaDto.setIdGerente(UUID.fromString(gerenteTransfer.getMessage()));
                    contaDto.setDataCriacao(new Date());

                    if (clienteDto.getSalario() >= 2000) {
                        contaDto.setLimite(clienteDto.getSalario() / 2);
                    } else {
                        contaDto.setLimite(0);
                    }

                    contaDto.setSaldo(0);
                    contaDto.setAtivo(false);

                    ContaTransfer contaTransfer = contaProducer.sendAndReceive(contaDto, "save-conta");

                    if (contaTransfer.getAction().equals("success-conta")) {

                        UUID uuid = UUID.randomUUID();
                        String password = uuid.toString().replaceAll("-", "");

                        UserDto userDto= new UserDto();

                        userDto.setNome(clienteDto.getNome());
                        userDto.setEmail(clienteDto.getEmail());
                        userDto.setSenha(password);
                        userDto.setCargo("CLIENTE");

                        UserTransfer userTransfer = userProducer.sendAndReceive(userDto, "save-user");

                        if (userTransfer.getAction().equals("success-user")) {

                            gerenteTransfer = gerenteProducer.sendAndReceive(gerenteTransfer.getMessage(), "add-one-cliente");

                            if (gerenteTransfer.getAction().equals("success-gerente")) {

                                return ResponseEntity.status(HttpStatus.CREATED).body("Cliente, Conta e Usuário criados com sucesso!");

                            } else if (gerenteTransfer.getAction().equals("failed-gerente")) {
                                clienteTransfer = clienteProducer.sendAndReceive(clienteTransfer.getMessage(), "delete-cliente");
                                contaTransfer = contaProducer.sendAndReceive(clienteTransfer.getMessage(), "delete-conta");
                                userTransfer = userProducer.sendAndReceive(clienteDto.getEmail(), "delete-user");
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        gerenteTransfer.getMessage() + ","
                                                + userTransfer.getMessage() + ", "
                                                + contaTransfer.getMessage() + " e "
                                                + clienteTransfer.getMessage()
                                );
                            }

                        } else if (userTransfer.getAction().equals("failed-user")) {
                            clienteTransfer = clienteProducer.sendAndReceive(clienteTransfer.getMessage(), "delete-cliente");
                            contaTransfer = contaProducer.sendAndReceive(clienteTransfer.getMessage(), "delete-conta");
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                    userTransfer.getMessage() + ", "
                                            + contaTransfer.getMessage() + " e "
                                            + clienteTransfer.getMessage()
                            );
                        }

                    } else if (contaTransfer.getAction().equals("failed-conta")) {
                        clienteTransfer = clienteProducer.sendAndReceive(clienteTransfer.getMessage(), "delete-cliente");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                contaTransfer.getMessage() + " e " + clienteTransfer.getMessage()
                        );
                    }

                } else if (clienteTransfer.getAction().equals("failed-cliente")) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(clienteTransfer.getMessage());
                }

            } else if (gerenteTransfer.getAction().equals("failed-gerente")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(gerenteTransfer.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao criar o cliente");
    }

    @PutMapping("/cliente/{id}")
    public ResponseEntity<Object> updateCliente(@PathVariable(value = "id") UUID id, @RequestBody ClienteDto clienteDto) {
        try {
            ContaDto contaDto = new ContaDto();

            if (clienteDto.getSalario() >= 2000) {
                contaDto.setLimite(clienteDto.getSalario() / 2);
            } else {
                contaDto.setLimite(0);
            }

            ContaTransfer contaTransfer = contaProducer.sendAndReceive(contaDto, id.toString(), "update-limite");

            if (contaTransfer.getAction().equals("success-conta")) {

                ClienteTransfer clienteTransfer = clienteProducer.sendAndReceive(clienteDto, id.toString(), "update-cliente");

                if (clienteTransfer.getAction().equals("success-cliente")) {

                    UserDto userDto = new UserDto();

                    userDto.setEmail(clienteDto.getEmail());

                    UserTransfer userTransfer = userProducer.sendAndReceive(
                            userDto,
                            clienteTransfer.getMessage(),
                            "update-user"
                    );

                    if (userTransfer.getAction().equals("success-user")) {

                        return ResponseEntity.status(HttpStatus.OK).body("Cliente atualizado com sucesso!");

                    } else if (userTransfer.getAction().equals("failed-user")) {
                        clienteDto.setEmail(clienteTransfer.getMessage());
                        clienteTransfer = clienteProducer.sendAndReceive(
                                clienteDto,
                                id.toString(),
                                "update-cliente"
                        );
                        contaDto.setLimite(Double.parseDouble(contaTransfer.getMessage()));
                        contaTransfer = contaProducer.sendAndReceive(contaDto, id.toString(), "update-limite");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                userTransfer.getMessage() + ", "
                                        + clienteTransfer.getMessage() + "e "
                                        + contaTransfer.getMessage()
                        );
                    }

                } else if (clienteTransfer.getAction().equals("failed-cliente")) {
                    contaDto.setLimite(Double.parseDouble(contaTransfer.getMessage()));
                    contaTransfer = contaProducer.sendAndReceive(contaDto, id.toString(), "update-limite");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            clienteTransfer.getMessage() + ", "
                                    + contaTransfer.getMessage()
                    );
                }

            } else if (contaTransfer.getAction().equals("failed-conta")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(contaTransfer.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao atualizar o cliente");
    }

    @DeleteMapping("/cliente/{id}")
    public ResponseEntity<Object> deleteCliente(@PathVariable(value = "id") UUID id) {
        try {
            ContaTransfer contaTransfer = contaProducer.sendAndReceive(id.toString(), "delete-conta");

            if (contaTransfer.getAction().equals("success-conta")) {

                GerenteTransfer gerenteTransfer = gerenteProducer.sendAndReceive(
                        contaTransfer.getContaDto().getIdGerente().toString(),
                        "sub-one-cliente"
                );

                if (gerenteTransfer.getAction().equals("success-gerente")) {

                    ClienteTransfer clienteTransfer = clienteProducer.sendAndReceive(id.toString(), "delete-cliente");

                    if (clienteTransfer.getAction().equals("success-cliente")) {

                        UserTransfer userTransfer = userProducer.sendAndReceive(clienteTransfer.getClienteDto().getEmail(), "delete-user");

                        if (userTransfer.getAction().equals("success-user")) {

                            return ResponseEntity.status(HttpStatus.OK).body("Cliente, Conta e Usuário deletados com sucesso!");

                        } else if (userTransfer.getAction().equals("failed-user")) {
                            clienteTransfer = clienteProducer.sendAndReceive(clienteTransfer.getClienteDto(), "save-cliente");
                            gerenteTransfer = gerenteProducer.sendAndReceive(
                                    contaTransfer.getContaDto().getIdGerente().toString(),
                                    "add-one-cliente"
                            );
                            contaTransfer = contaProducer.sendAndReceive(contaTransfer.getContaDto(), "save-conta");
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                    userTransfer.getMessage() + ", "
                                            + clienteTransfer.getMessage() + ", "
                                            + gerenteTransfer.getMessage() + "e "
                                            + contaTransfer.getMessage()
                            );
                        }

                    } else if (clienteTransfer.getAction().equals("failed-cliente")) {
                        gerenteTransfer = gerenteProducer.sendAndReceive(
                                contaTransfer.getContaDto().getIdGerente().toString(),
                                "add-one-cliente"
                        );
                        contaTransfer = contaProducer.sendAndReceive(contaTransfer.getContaDto(), "save-conta");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                clienteTransfer.getMessage() + ", "
                                        + gerenteTransfer.getMessage() + "e "
                                        + contaTransfer.getMessage()
                        );
                    }

                } else if (gerenteTransfer.getAction().equals("failed-gerente")) {
                    contaTransfer = contaProducer.sendAndReceive(contaTransfer.getContaDto(), "save-conta");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            gerenteTransfer.getMessage() + ", "
                                    + contaTransfer.getMessage()
                    );
                }

            } else if (contaTransfer.getAction().equals("failed-conta")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(contaTransfer.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao criar a conta");
    }

    @PostMapping("/gerente")
    public ResponseEntity<Object> saveGerente(@RequestBody GerenteDto gerenteDto) {
        try {
            // PEGA O GERENTE COM MAIS CLIENTES
            GerenteTransfer gerenteTransfer = gerenteProducer.sendAndReceive("max-gerente");

            if (gerenteTransfer.getAction().equals("failed-gerente")) {
                // SALVA UM NOVO GERENTE
                // COMO NÃO VEIO NENHUM GERENTE NA CONSULTA ANTERIOR
                // SIGNIFICA QUE ESTAMOS LIDANDO COM O PRIMEIRO GERENTE
                // OU QUE NÃO HÁ CLIENTES CADASTRADOS
                gerenteDto.setNumeroClientes(0);
                gerenteTransfer = gerenteProducer.sendAndReceive(gerenteDto, "save-gerente");

                if (gerenteTransfer.getAction().equals("failed-gerente")) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(gerenteTransfer.getMessage());
                }

                // CRIA UM NOVO USUÁRIO PRO NOVO GERENTE
                UUID uuid = UUID.randomUUID();
                String password = uuid.toString().replaceAll("-", "");

                UserDto userDto= new UserDto();

                userDto.setNome(gerenteDto.getNome());
                userDto.setEmail(gerenteDto.getEmail());
                userDto.setSenha(password);
                userDto.setCargo("GERENTE");

                UserTransfer userTransfer = userProducer.sendAndReceive(userDto, "save-user");

                if (userTransfer.getAction().equals("failed-user")) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(userTransfer.getMessage());
                }

                return ResponseEntity.status(HttpStatus.CREATED).body("Gerente e Usuário criados com sucesso!");
            }

            // TIRA UM CLIENTE DESSE GERENTE QUE FOI PEGO
            String idGerente = gerenteTransfer.getMessage();
            gerenteTransfer = gerenteProducer.sendAndReceive(idGerente,  "sub-one-cliente");

            if (gerenteTransfer.getAction().equals("failed-gerente")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(gerenteTransfer.getMessage());
            }

            // SALVA UM NOVO GERENTE
            gerenteDto.setNumeroClientes(1);
            gerenteTransfer = gerenteProducer.sendAndReceive(gerenteDto, "save-gerente");

            if (gerenteTransfer.getAction().equals("failed-gerente")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(gerenteTransfer.getMessage());
            }

            // PEGA UMA DAS CONTAS DO GERENTE QUE TINHA MAIS CLIENTES
            // E ENTÃO ATUALIZA A REFERÊNCIA DESSA CONTA PARA APONTAR PARA O NOVO CLIENTE CADASTRADO
            String idAntigoAndAtual = idGerente + "+" + gerenteTransfer.getMessage();
            ContaTransfer contaTransfer = contaProducer.sendAndReceive(idAntigoAndAtual, "update-gerente");

            if (contaTransfer.getAction().equals("failed-conta")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(contaTransfer.getMessage());
            }

            // CRIA UM NOVO USUÁRIO PRO NOVO GERENTE
            UUID uuid = UUID.randomUUID();
            String password = uuid.toString().replaceAll("-", "");

            UserDto userDto= new UserDto();

            userDto.setNome(gerenteDto.getNome());
            userDto.setEmail(gerenteDto.getEmail());
            userDto.setSenha(password);
            userDto.setCargo("GERENTE");

            UserTransfer userTransfer = userProducer.sendAndReceive(userDto, "save-user");

            if (userTransfer.getAction().equals("failed-user")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(userTransfer.getMessage());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Gerente e Usuário criados com sucesso!");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao criar o gerente");
        }
    }


    @PostMapping("/transacao")
    public ResponseEntity<Object> saveTransacao(@RequestBody TransacaoDto transacaoDto) {
        try {
            TransacaoTransfer transacaoTransfer = transacaoProducer.sendAndReceive(transacaoDto, "save-transacao");

            if (transacaoTransfer.getAction().equals("success-transacao")) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Transacao criada com sucesso!");
            } else if (transacaoTransfer.getAction().equals("failed-transacao")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(transacaoTransfer.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao criar a transacao");
    }
}
