package com.pd.swiftchat.repository;

import com.pd.swiftchat.model.Processo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessoRepository extends JpaRepository<Processo, Long> {
    Optional<Processo> findFirstByUsuario(String usuario);
}
