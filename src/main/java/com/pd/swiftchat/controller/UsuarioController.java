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
        // Validação de CPF e CNPJ
        if (usuario.getCpf() != null && usuario.getCpf().length() != 11) {
            throw new IllegalArgumentException("O CPF deve conter exatamente 11 dígitos.");
        }
        if (usuario.getCnpj() != null && usuario.getCnpj().length() != 14) {
            throw new IllegalArgumentException("O CNPJ deve conter exatamente 14 dígitos.");
        }

        // Defina o tipo de pessoa (FISICA ou JURIDICA) corretamente com base nos valores recebidos.
        if (usuario.getTipoUsuario() == 2) {  // Funcionário
            // Funcionário só pode ser Pessoa Física (com CPF)
            if (usuario.getCpf() == null) {
                throw new IllegalArgumentException("Funcionário deve ter CPF.");
            }
            usuario.setTipoPessoa("FISICA");  // Definindo como Pessoa Física
        } else if (usuario.getTipoUsuario() == 1) {  // Usuário comum
            // Usuário pode ser Pessoa Física ou Jurídica
            if (usuario.getCpf() != null) {
                usuario.setTipoPessoa("FISICA");
            } else if (usuario.getCnpj() != null) {
                usuario.setTipoPessoa("JURIDICA");
            } else {
                throw new IllegalArgumentException("Usuário deve ter CPF ou CNPJ.");
            }
        } else {
            throw new IllegalArgumentException("Tipo de usuário inválido.");
        }

        // Validação de nome e sobrenome
        if (!validarNome(usuario.getNome()) || (usuario.getTipoPessoa() != null && usuario.getTipoPessoa().equals("FISICA") && !validarNome(usuario.getSobrenome()))) {
            throw new IllegalArgumentException("Nome e sobrenome devem conter apenas letras e começar com letra maiúscula.");
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

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        Usuario novoUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.ok(novoUsuario);
    }

    private boolean validarNome(String nome) {
        return nome.matches("^[A-Z][a-zA-ZÀ-ÿ\\s]+$");  // Nome deve começar com letra maiúscula e conter apenas letras
    }

}
