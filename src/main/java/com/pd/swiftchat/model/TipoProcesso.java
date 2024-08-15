package com.pd.swiftchat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "tipo_processo")
@Getter
@Setter
public class TipoProcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @OneToMany(mappedBy = "tipoProcesso", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Processo> processos;
}