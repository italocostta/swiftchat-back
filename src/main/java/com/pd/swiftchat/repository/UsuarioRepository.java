package com.pd.swiftchat.repository;

import com.pd.swiftchat.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCpf(String cpf);

    Optional<Usuario> findByCnpj(String cnpj);
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.tipoUsuario = 1")
    Long countUsuarios(); // Conta apenas os usuários (tipoUsuario = 1)
}
