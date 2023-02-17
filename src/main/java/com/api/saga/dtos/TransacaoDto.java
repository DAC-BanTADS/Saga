package com.api.saga.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.UUID;

public class TransacaoDto {
    @NotNull
    private Date data;
    @NotNull
    private double valorTransacao;
    @NotNull
    private UUID idCliente;
    @NotBlank
    private String tipoTransacao;
    @NotNull
    private UUID idClienteDestinatario;
    @NotNull
    private double saldo;
    private String color;

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public double getValorTransacao() {
        return valorTransacao;
    }

    public void setValorTransacao(double valorTransacao) {
        this.valorTransacao = valorTransacao;
    }

    public UUID getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(UUID idCliente) {
        this.idCliente = idCliente;
    }

    public String getTipoTransacao() {
        return tipoTransacao;
    }

    public void setTipoTransacao(String tipoTransacao) {
        this.tipoTransacao = tipoTransacao;
    }

    public UUID getIdClienteDestinatario() {
        return idClienteDestinatario;
    }

    public void setIdClienteDestinatario(UUID idClienteDestinatario) {
        this.idClienteDestinatario = idClienteDestinatario;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}