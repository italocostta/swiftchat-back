package com.pd.swiftchat.dto;

import com.pd.swiftchat.model.Setor;
import com.pd.swiftchat.model.TipoUsuario;

public class UsuarioDTO {

    private Long id;
    private String nome;
    private String cpfCnpj;
    private String senha;
    private TipoUsuario tipoUsuario;
    private Setor setor;

    // Construtores
    public UsuarioDTO() {}

    public UsuarioDTO(Long id, String nome, String cpfCnpj, String senha, TipoUsuario tipoUsuario, Setor setor) {
        this.id = id;
        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.senha = senha;
        this.tipoUsuario = tipoUsuario;
        this.setor = setor;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public Setor getSetor() {
        return setor;
    }

    public void setSetor(Setor setor) {
        this.setor = setor;
    }
}
