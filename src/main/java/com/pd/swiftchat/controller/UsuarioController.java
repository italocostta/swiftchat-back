package com.pd.swiftchat.controller;

import com.pd.swiftchat.exception.CpfCnpjJaUtilizadoException;
import com.pd.swiftchat.model.Usuario;
import com.pd.swiftchat.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<Usuario> registerUser(@RequestBody Usuario usuario) {
        // Validação: Certifique-se de que o CPF ou CNPJ está presente
        if (usuario.getCpf() == null && usuario.getCnpj() == null) {
            throw new IllegalArgumentException("CPF ou CNPJ deve ser fornecido.");
        }

        // Verificação se já existe um usuário com o mesmo CPF ou CNPJ
        Optional<Usuario> existingUsuario;

        if (usuario.getCpf() != null) {
            existingUsuario = usuarioRepository.findByCpf(usuario.getCpf());
        } else {
            existingUsuario = usuarioRepository.findByCnpj(usuario.getCnpj());
        }

        if (existingUsuario.isPresent()) {
            throw new CpfCnpjJaUtilizadoException("Usuário com o mesmo CPF ou CNPJ já existe.");
        }

        // Codificando a senha antes de salvar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // NÃO PRECISA DEFINIR AUTHORITIES AQUI
        // O método getAuthorities() na classe Usuario será suficiente

        // Salvando o novo usuário
        Usuario novoUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.ok(novoUsuario);
    }

}
