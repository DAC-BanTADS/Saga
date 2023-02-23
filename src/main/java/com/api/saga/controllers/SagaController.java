package com.api.saga.controllers;

import com.api.saga.amqp.*;
import com.api.saga.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Objects;
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

            if (!Objects.isNull(gerenteTransfer) && gerenteTransfer.getAction().equals("success-gerente")) {

                ClienteTransfer clienteTransfer = clienteProducer.sendAndReceive(clienteDto, "save-cliente");

                if (!Objects.isNull(clienteTransfer) && clienteTransfer.getAction().equals("success-cliente")) {

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

                    if (!Objects.isNull(contaTransfer) && contaTransfer.getAction().equals("success-conta")) {

                        UUID uuid = UUID.randomUUID();
                        String password = uuid.toString().replaceAll("-", "");

                        UserDto userDto= new UserDto();

                        userDto.setNome(clienteDto.getNome());
                        userDto.setEmail(clienteDto.getEmail());
                        userDto.setSenha(password);
                        userDto.setCargo("CLIENTE");

                        UserTransfer userTransfer = userProducer.sendAndReceive(userDto, "save-user");

                        if (!Objects.isNull(userTransfer) && userTransfer.getAction().equals("success-user")) {

                            gerenteTransfer = gerenteProducer.sendAndReceive(gerenteTransfer.getMessage(), "add-one-cliente");

                            if (!Objects.isNull(gerenteTransfer) && gerenteTransfer.getAction().equals("success-gerente")) {

                                return ResponseEntity.status(HttpStatus.CREATED).body("Cliente, Conta e Usuário criados com sucesso!");

                            } else if (Objects.isNull(gerenteTransfer) || gerenteTransfer.getAction().equals("failed-gerente")) {
                                clienteTransfer = clienteProducer.sendAndReceive(clienteTransfer.getMessage(), "delete-cliente");
                                contaTransfer = contaProducer.sendAndReceive(clienteTransfer.getMessage(), "delete-conta");
                                userTransfer = userProducer.sendAndReceive(clienteDto.getEmail(), "delete-user");
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        gerenteTransfer.getMessage()
                                );
                            }

                        } else if (Objects.isNull(userTransfer) || userTransfer.getAction().equals("failed-user")) {
                            clienteTransfer = clienteProducer.sendAndReceive(clienteTransfer.getMessage(), "delete-cliente");
                            contaTransfer = contaProducer.sendAndReceive(clienteTransfer.getMessage(), "delete-conta");
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                    userTransfer.getMessage()
                            );
                        }

                    } else if (Objects.isNull(contaTransfer) || contaTransfer.getAction().equals("failed-conta")) {
                        clienteTransfer = clienteProducer.sendAndReceive(clienteTransfer.getMessage(), "delete-cliente");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                contaTransfer.getMessage()
                        );
                    }

                } else if (Objects.isNull(clienteTransfer) || clienteTransfer.getAction().equals("failed-cliente")) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            clienteTransfer.getMessage()
                    );
                }

            } else if (Objects.isNull(gerenteTransfer) || gerenteTransfer.getAction().equals("failed-gerente")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        gerenteTransfer.getMessage()
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao criar o cliente: " + e.getMessage());
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

            if (!Objects.isNull(contaTransfer) && contaTransfer.getAction().equals("success-conta")) {

                ClienteTransfer clienteTransfer = clienteProducer.sendAndReceive(clienteDto, id.toString(), "update-cliente");

                if (!Objects.isNull(clienteTransfer) && clienteTransfer.getAction().equals("success-cliente")) {

                    UserDto userDto = new UserDto();

                    userDto.setEmail(clienteDto.getEmail());

                    UserTransfer userTransfer = userProducer.sendAndReceive(
                            userDto,
                            clienteTransfer.getMessage(),
                            "update-user"
                    );

                    if (!Objects.isNull(userTransfer) && userTransfer.getAction().equals("success-user")) {

                        return ResponseEntity.status(HttpStatus.OK).body("Cliente atualizado com sucesso!");

                    } else if (Objects.isNull(userTransfer) || userTransfer.getAction().equals("failed-user")) {
                        clienteDto.setEmail(clienteTransfer.getMessage());
                        clienteTransfer = clienteProducer.sendAndReceive(
                                clienteDto,
                                id.toString(),
                                "update-cliente"
                        );
                        contaDto.setLimite(Double.parseDouble(contaTransfer.getMessage()));
                        contaTransfer = contaProducer.sendAndReceive(contaDto, id.toString(), "update-limite");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                contaTransfer.getMessage()
                        );
                    }

                } else if (Objects.isNull(clienteTransfer) || clienteTransfer.getAction().equals("failed-cliente")) {
                    contaDto.setLimite(Double.parseDouble(contaTransfer.getMessage()));
                    contaTransfer = contaProducer.sendAndReceive(contaDto, id.toString(), "update-limite");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            clienteTransfer.getMessage()
                    );
                }

            } else if (Objects.isNull(contaTransfer) || contaTransfer.getAction().equals("failed-conta")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        contaTransfer.getMessage()
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao atualizar o cliente: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao atualizar o cliente");
    }

    @DeleteMapping("/cliente/{id}")
    public ResponseEntity<Object> deleteCliente(@PathVariable(value = "id") UUID id) {
        try {
            ContaTransfer contaTransfer = contaProducer.sendAndReceive(id.toString(), "delete-conta");

            if (!Objects.isNull(contaTransfer) && contaTransfer.getAction().equals("success-conta")) {

                GerenteTransfer gerenteTransfer = gerenteProducer.sendAndReceive(
                        contaTransfer.getContaDto().getIdGerente().toString(),
                        "sub-one-cliente"
                );

                if (!Objects.isNull(gerenteTransfer) && gerenteTransfer.getAction().equals("success-gerente")) {

                    ClienteTransfer clienteTransfer = clienteProducer.sendAndReceive(id.toString(), "delete-cliente");

                    if (!Objects.isNull(clienteTransfer) && clienteTransfer.getAction().equals("success-cliente")) {

                        UserTransfer userTransfer = userProducer.sendAndReceive(clienteTransfer.getClienteDto().getEmail(), "delete-user");

                        if (!Objects.isNull(userTransfer) && userTransfer.getAction().equals("success-user")) {

                            return ResponseEntity.status(HttpStatus.OK).body("Cliente, Conta e Usuário deletados com sucesso!");

                        } else if (Objects.isNull(userTransfer) || userTransfer.getAction().equals("failed-user")) {
                            clienteTransfer = clienteProducer.sendAndReceive(clienteTransfer.getClienteDto(), "save-cliente");
                            gerenteTransfer = gerenteProducer.sendAndReceive(
                                    contaTransfer.getContaDto().getIdGerente().toString(),
                                    "add-one-cliente"
                            );
                            contaTransfer = contaProducer.sendAndReceive(contaTransfer.getContaDto(), "save-conta");
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                    contaTransfer.getMessage()
                            );
                        }

                    } else if (Objects.isNull(clienteTransfer) || clienteTransfer.getAction().equals("failed-cliente")) {
                        gerenteTransfer = gerenteProducer.sendAndReceive(
                                contaTransfer.getContaDto().getIdGerente().toString(),
                                "add-one-cliente"
                        );
                        contaTransfer = contaProducer.sendAndReceive(contaTransfer.getContaDto(), "save-conta");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                contaTransfer.getMessage()
                        );
                    }

                } else if (Objects.isNull(gerenteTransfer) || gerenteTransfer.getAction().equals("failed-gerente")) {
                    contaTransfer = contaProducer.sendAndReceive(contaTransfer.getContaDto(), "save-conta");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            gerenteTransfer.getMessage()
                    );
                }

            } else if (Objects.isNull(contaTransfer) || contaTransfer.getAction().equals("failed-conta")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        contaTransfer.getMessage()
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao deletar o cliente: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao deletar o cliente");
    }

    @PostMapping("/gerente")
    public ResponseEntity<Object> saveGerente(@RequestBody GerenteDto gerenteDto) {
        try {
            // PEGA O GERENTE COM MAIS CLIENTES
            GerenteTransfer gerenteTransfer = gerenteProducer.sendAndReceive("max-gerente");

            if (Objects.isNull(gerenteTransfer) || gerenteTransfer.getAction().equals("failed-gerente")) {
                // SALVA UM NOVO GERENTE
                // COMO NÃO VEIO NENHUM GERENTE NA CONSULTA ANTERIOR
                // SIGNIFICA QUE ESTAMOS LIDANDO COM O PRIMEIRO GERENTE
                // OU QUE NÃO HÁ CLIENTES CADASTRADOS
                gerenteDto.setNumeroClientes(0);
                gerenteTransfer = gerenteProducer.sendAndReceive(gerenteDto, "save-gerente");

                if (Objects.isNull(gerenteTransfer) || gerenteTransfer.getAction().equals("failed-gerente")) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            gerenteTransfer.getMessage()
                    );
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

                if (Objects.isNull(userTransfer) || userTransfer.getAction().equals("failed-user")) {
                    // EXCLUIR O GERENTE QUE FOI CRIADO ANTES
                    gerenteProducer.sendAndReceive(gerenteTransfer.getMessage(), "delete-gerente");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            userTransfer.getMessage()
                    );
                }

                return ResponseEntity.status(HttpStatus.CREATED).body("Gerente e Usuário criados com sucesso!");
            }

            // TIRA UM CLIENTE DESSE GERENTE QUE FOI PEGO
            String idGerente = gerenteTransfer.getMessage();
            gerenteTransfer = gerenteProducer.sendAndReceive(idGerente, "sub-one-cliente");

            if (Objects.isNull(gerenteTransfer) || gerenteTransfer.getAction().equals("failed-gerente")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        gerenteTransfer.getMessage()
                );
            }

            // SALVA UM NOVO GERENTE
            gerenteDto.setNumeroClientes(1);
            gerenteTransfer = gerenteProducer.sendAndReceive(gerenteDto, "save-gerente");

            if (Objects.isNull(gerenteTransfer) || gerenteTransfer.getAction().equals("failed-gerente")) {
                // ADICIONAR UM CLIENTE AO GERENTE QUE TINHA PERDIDO UM
                gerenteProducer.sendAndReceive(idGerente, "add-one-cliente");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        gerenteTransfer.getMessage()
                );
            }

            // PEGA UMA DAS CONTAS DO GERENTE QUE TINHA MAIS CLIENTES
            // E ENTÃO ATUALIZA A REFERÊNCIA DESSA CONTA PARA APONTAR PARA O NOVO CLIENTE CADASTRADO
            String idAntigoAndAtual = idGerente + "+" + gerenteTransfer.getMessage();
            ContaTransfer contaTransfer = contaProducer.sendAndReceive(idAntigoAndAtual, "update-gerente");

            if (Objects.isNull(contaTransfer) || contaTransfer.getAction().equals("failed-conta")) {
                // ADICIONAR UM CLIENTE AO GERENTE QUE TINHA PERDIDO UM
                gerenteProducer.sendAndReceive(idGerente, "add-one-cliente");
                // REMOVER O GERENTE QUE FOI CRIADO
                gerenteProducer.sendAndReceive(gerenteTransfer.getMessage(), "delete-gerente");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        contaTransfer.getMessage()
                );
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

            if (Objects.isNull(userTransfer) || userTransfer.getAction().equals("failed-user")) {
                // ADICIONAR UM CLIENTE AO GERENTE QUE TINHA PERDIDO UM
                gerenteProducer.sendAndReceive(idGerente, "add-one-cliente");
                // REMOVER O GERENTE QUE FOI CRIADO
                gerenteProducer.sendAndReceive(gerenteTransfer.getMessage(), "delete-gerente");
                // REAPONTA A CONTA PARA O ANTIGO GERENTE
                idAntigoAndAtual = gerenteTransfer.getMessage() + "+" + idGerente;
                contaProducer.sendAndReceive(idAntigoAndAtual, "update-gerente");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        userTransfer.getMessage()
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Gerente e Usuário criados com sucesso!");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao criar o gerente: " + e.getMessage());
        }
    }

    @PutMapping("/gerente/{id}")
    public ResponseEntity<Object> updateGerente(@PathVariable(value = "id") UUID id, @RequestBody GerenteDto gerenteDto) {
        try {
            GerenteTransfer gerenteTransfer = gerenteProducer.sendAndReceive(gerenteDto, id.toString(), "update-gerente");

            if (!Objects.isNull(gerenteTransfer) && gerenteTransfer.getAction().equals("success-gerente")) {

                UserDto userDto = new UserDto();

                userDto.setEmail(gerenteDto.getEmail());

                UserTransfer userTransfer = userProducer.sendAndReceive(
                        userDto,
                        gerenteTransfer.getMessage(),
                        "update-user"
                );

                if (!Objects.isNull(userTransfer) && userTransfer.getAction().equals("success-user")) {

                    return ResponseEntity.status(HttpStatus.OK).body("Gerente atualizado com sucesso!");

                } else if (Objects.isNull(userTransfer) || userTransfer.getAction().equals("failed-user")) {
                    gerenteDto.setEmail(gerenteTransfer.getMessage());
                    gerenteTransfer = gerenteProducer.sendAndReceive(
                            gerenteDto,
                            id.toString(),
                            "update-gerente"
                    );
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            userTransfer.getMessage()
                    );
                }

            } else if (Objects.isNull(gerenteTransfer) || gerenteTransfer.getAction().equals("failed-gerente")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        gerenteTransfer.getMessage()
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao atualizar o gerente: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao atualizar o gerente");
    }

    @DeleteMapping("/gerente/{id}")
    public ResponseEntity<Object> deleteGerente(@PathVariable(value = "id") UUID id) {
        try {
            // PROCURAR O GERENTE QUE TÁ SENDO PASSADO PARA DELETAR
            GerenteTransfer gerenteTransferDelete = gerenteProducer.sendAndReceive(id.toString(), "get-gerente");

            if (Objects.isNull(gerenteTransferDelete) || gerenteTransferDelete.getAction().equals("failed-gerente")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        gerenteTransferDelete.getMessage()
                );
            }

            GerenteTransfer qtdGerentes = gerenteProducer.sendAndReceive("get-number-gerente");

            if (!Objects.isNull(qtdGerentes) && qtdGerentes.getAction().equals("success-gerente")) {
                if (Integer.parseInt(qtdGerentes.getMessage()) == 1
                    && gerenteTransferDelete.getGerenteDto().getNumeroClientes() > 0) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            "Não é possível deletar o último gerente que possui clientes."
                    );
                }
            }

            // CONSULTAR GERENTE COM MENOS CONTAS
            GerenteTransfer gerenteTransfer = gerenteProducer.sendAndReceive("min-gerente");

            if (Objects.isNull(gerenteTransfer)
                    || gerenteTransfer.getAction().equals("failed-gerente")
                    || gerenteTransferDelete.getGerenteDto().getNumeroClientes() == 0) {
                // REMOVE O GERENTE PASSADO
                gerenteTransfer = gerenteProducer.sendAndReceive(id.toString(), "delete-gerente");

                if (Objects.isNull(gerenteTransfer) || gerenteTransfer.getAction().equals("failed-gerente")) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            gerenteTransfer.getMessage()
                    );
                }

                // REMOVE O USUÁRIO DO GERENTE
                UserTransfer userTransfer = userProducer.sendAndReceive(gerenteTransfer.getGerenteDto().getEmail(), "delete-user");

                if (Objects.isNull(userTransfer) || userTransfer.getAction().equals("failed-user")) {
                    // READICIONA O GERENTE QUE FOI EXCLUÍDO
                    gerenteProducer.sendAndReceive(gerenteTransfer.getGerenteDto(), "save-gerente");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            userTransfer.getMessage()
                    );
                }

                return ResponseEntity.status(HttpStatus.CREATED).body("Gerente e Usuário deletados com sucesso!");
            }

            // SOMA UM CLIENTE DESSE GERENTE QUE FOI PEGO
            String idGerente = gerenteTransfer.getMessage();
            String idAntigoAndAtual = id + "+" + idGerente;
            gerenteTransfer = gerenteProducer.sendAndReceive(idAntigoAndAtual,  "add-cliente");

            if (Objects.isNull(gerenteTransfer) || gerenteTransfer.getAction().equals("failed-gerente")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        gerenteTransfer.getMessage()
                );
            }

            // REMOVE O GERENTE PASSADO
            gerenteTransfer = gerenteProducer.sendAndReceive(id.toString(), "delete-gerente");

            if (Objects.isNull(gerenteTransfer) || gerenteTransfer.getAction().equals("failed-gerente")) {
                // DIMINUIR UM CLIENTE DO QUE FOI ADICIONADO
                gerenteProducer.sendAndReceive(idGerente,  "sub-one-cliente");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        gerenteTransfer.getMessage()
                );
            }

            // ATRIBUIR AS CONTAS ASSOCIADAS AO GERENTE QUE VAMOS EXCLUIR
            // AO GERENTE QUE TEM MENOS CLIENTES, PARA QUE AS CONTAS NÃO FIQUEM SEM GERÊNCIA
            ContaTransfer contaTransfer = contaProducer.sendAndReceive(idAntigoAndAtual, "update-gerente-delete");

            if (Objects.isNull(contaTransfer) || contaTransfer.getAction().equals("failed-conta")) {
                // DIMINUIR UM CLIENTE DO QUE FOI ADICIONADO
                gerenteProducer.sendAndReceive(idGerente,  "sub-one-cliente");
                // READICIONAR O GERENTE PASSADO
                gerenteProducer.sendAndReceive(gerenteTransfer.getGerenteDto(), "save-gerente");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        contaTransfer.getMessage()
                );
            }

            // REMOVE O USUÁRIO DO GERENTE
            UserTransfer userTransfer = userProducer.sendAndReceive(gerenteTransfer.getGerenteDto().getEmail(), "delete-user");

            if (Objects.isNull(userTransfer) || userTransfer.getAction().equals("failed-user")) {
                // DIMINUIR UM CLIENTE DO QUE FOI ADICIONADO
                gerenteProducer.sendAndReceive(idGerente,  "sub-one-cliente");
                // READICIONAR O GERENTE PASSADO
                gerenteProducer.sendAndReceive(gerenteTransfer.getGerenteDto(), "save-gerente");
                // REATRIBUIR AS REFERÊNCIAS DE CONTA
                idAntigoAndAtual = idGerente + "+" + id;
                contaProducer.sendAndReceive(idAntigoAndAtual, "update-gerente");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        userTransfer.getMessage()
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Gerente e Usuário deletados com sucesso!");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao deletar o gerente: " + e.getMessage());
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
