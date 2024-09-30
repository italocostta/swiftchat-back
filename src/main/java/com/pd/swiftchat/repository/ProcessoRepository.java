package com.pd.swiftchat.repository;

import com.pd.swiftchat.model.Processo;
import com.pd.swiftchat.model.Setor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessoRepository extends JpaRepository<Processo, Long> {
    List<Processo> findByCpf(String cpf);
    List<Processo> findByCnpj(String cnpj);
    List<Processo> findBySetor(Setor setor);
}