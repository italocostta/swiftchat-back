package com.pd.swiftchat.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.Collections;

@Entity
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    private String cpf;

    @Getter
    @Setter
    private String cnpj;

    @Getter
    @Setter
    private String nome;

    @Getter
    @Setter
    @Column(nullable = true)
    private String sobrenome;  // Sobrenome obrigatório para pessoa física

    @Getter
    @Setter
    private String razaoSocial;  // Razão social para pessoa jurídica

    @Getter
    @Setter
    @Column(nullable = true)
    private String matricula;  // Campo de matrícula para funcionários

    @Setter
    private String password;

    @Getter
    @Setter
    @Column(name = "tipo_pessoa", columnDefinition = "varchar(10)")  // "FISICA" ou "JURIDICA"
    private String tipoPessoa;

    @Column(name = "tipo_usuario", columnDefinition = "smallint")
    @Getter
    @Setter
    private int tipoUsuario;  // 1 = Usuario, 2 = Funcionario

    // Adicionando o relacionamento com Setor
    @ManyToOne
    @JoinColumn(name = "setor_id")
    @Getter
    @Setter
    private Setor setor;  // Setor ao qual o funcionário pertence (se for funcionário)

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.isUsuarioComum()) {
            return Collections.singletonList(new SimpleGrantedAuthority("USUARIO"));
        } else if (this.isFuncionario()) {
            return Collections.singletonList(new SimpleGrantedAuthority("FUNCIONARIO"));
        }
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.cpf != null ? this.cpf : this.cnpj;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Método auxiliar para verificar se é um usuário comum
    public boolean isUsuarioComum() {
        return this.tipoUsuario == 1;
    }

    // Método auxiliar para verificar se é um funcionário
    public boolean isFuncionario() {
        return this.tipoUsuario == 2;
    }

    // Método para retornar uma string representando o tipo de usuário
    public String getTipoUsuarioDescricao() {
        return this.tipoUsuario == 1 ? "Usuário Comum" : "Funcionário";
    }
}
