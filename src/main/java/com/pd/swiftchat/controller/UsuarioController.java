package com.pd.swiftchat.controller;

import com.pd.swiftchat.exception.CpfCnpjJaUtilizadoException;
import com.pd.swiftchat.model.Usuario;
import com.pd.swiftchat.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

        Usuario novoUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.ok(novoUsuario);
    }
}
