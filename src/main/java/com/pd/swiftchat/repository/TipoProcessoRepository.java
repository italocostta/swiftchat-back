package com.pd.swiftchat.repository;

import com.pd.swiftchat.model.TipoProcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoProcessoRepository extends JpaRepository<TipoProcesso, Long> { }
