package com.pd.swiftchat.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Entity
@Table(name = "processo")
@Getter
@Setter
public class Processo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, unique = true)  // Permitir nulos temporariamente
    private Integer numeroProcesso;

    @Column(nullable = false)
    private String usuario;

    @ManyToOne
    @JoinColumn(name = "tipo_processo_id", nullable = false)
    private TipoProcesso tipoProcesso;

    @PrePersist
    public void generateNumeroProcesso() {
        if (this.numeroProcesso == null) {
            Random random = new Random();
            this.numeroProcesso = 10000 + random.nextInt(90000);
        }
    }
}