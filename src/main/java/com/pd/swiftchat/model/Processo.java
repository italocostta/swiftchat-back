package com.pd.swiftchat.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Entity
@Table(name = "processo")
@Getter
@Setter
public class Processo implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, unique = true)
    private Integer numeroProcesso;

    @Column(nullable = false)
    private String usuario;

    @Column(nullable = true)  // Permitir nulo, pois será usado para pessoa física
    private String cpf;

    @Column(nullable = true)  // Permitir nulo, pois será usado para pessoa jurídica
    private String cnpj;

    @Column(nullable = false)
    private String tipoPessoa; // "FISICA" ou "JURIDICA"

    @ManyToOne
    @JoinColumn(name = "tipo_processo_id", nullable = false)
    private TipoProcesso tipoProcesso;

    @ManyToOne
    @JoinColumn(name = "setor_id", nullable = true)
    private Setor setor;

    @Column(nullable = false)
    private String arquivo;

    @Column(name = "status_processo", nullable = false)
    private String statusProcesso;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @ElementCollection
    @CollectionTable(name = "processo_arquivos", joinColumns = @JoinColumn(name = "processo_id"))
    @Column(name = "arquivo")
    private List<String> arquivos = new ArrayList<>();

    @PrePersist
    public void generateNumeroProcesso() {
        if (this.numeroProcesso == null) {
            Random random = new Random();
            this.numeroProcesso = 10000 + random.nextInt(90000);
        }
    }
}


