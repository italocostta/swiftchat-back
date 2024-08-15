package com.pd.swiftchat.model;

import jakarta.persistence.*;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;

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

    @Column(nullable = false, unique = true)
    private Integer numeroProcesso;

    @Column(nullable = false)
    private String usuario;

    @ManyToOne
    @JoinColumn(name = "tipo_processo_id", nullable = false)
    private TipoProcesso tipoProcesso;

    @ManyToOne
    @JoinColumn(name = "setor_id", nullable = true)
    private Setor setor;

    @PrePersist
    public void generateNumeroProcesso() {
        if (this.numeroProcesso == null) {
            Random random = new Random();
            this.numeroProcesso = 10000 + random.nextInt(90000);
        }
    }
}
