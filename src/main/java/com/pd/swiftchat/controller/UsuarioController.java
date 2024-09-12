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

        // Verificação se já existe um usuário com o mesmo CPF ou CNPJ
        Optional<Usuario> existingUsuario;

        if (usuario.getTipoUsuario() == 2) {  // Funcionário
            // Funcionário só pode ser Pessoa Física (com CPF)
            if (usuario.getCpf() == null) {
                throw new IllegalArgumentException("Funcionário deve ter CPF.");
            }
            if (usuario.getSobrenome() == null || usuario.getSobrenome().isEmpty()) {
                throw new IllegalArgumentException("Sobrenome é obrigatório para pessoa física.");
            }
            existingUsuario = usuarioRepository.findByCpf(usuario.getCpf());
            usuario.setTipoPessoa("FISICA");  // Funcionário sempre será Pessoa Física
            usuario.setRazaoSocial(null);  // Razão social não é aplicável para funcionários
        } else if (usuario.getTipoUsuario() == 1) {  // Usuário comum
            // Usuário pode ser Pessoa Física ou Jurídica
            if (usuario.getCpf() != null) {
                if (usuario.getSobrenome() == null || usuario.getSobrenome().isEmpty()) {
                    throw new IllegalArgumentException("Sobrenome é obrigatório para pessoa física.");
                }
                existingUsuario = usuarioRepository.findByCpf(usuario.getCpf());
                usuario.setTipoPessoa("FISICA");  // Definindo como FÍSICA
                usuario.setRazaoSocial(null);  // Razão social não é aplicável para pessoa física
            } else if (usuario.getCnpj() != null) {
                if (usuario.getRazaoSocial() == null || usuario.getRazaoSocial().isEmpty()) {
                    throw new IllegalArgumentException("Razão social é obrigatória para pessoa jurídica.");
                }
                existingUsuario = usuarioRepository.findByCnpj(usuario.getCnpj());
                usuario.setTipoPessoa("JURIDICA");  // Definindo como JURÍDICA
                usuario.setSobrenome(null);  // Sobrenome não é aplicável para pessoa jurídica
            } else {
                throw new IllegalArgumentException("Usuário deve ter CPF ou CNPJ.");
            }
        } else {
            throw new IllegalArgumentException("Tipo de usuário inválido.");
        }

        if (existingUsuario.isPresent()) {
            throw new CpfCnpjJaUtilizadoException("Usuário com o mesmo CPF ou CNPJ já existe.");
        }

        // Codificando a senha antes de salvar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Salvando o novo usuário
        Usuario novoUsuario = usuarioRepository.save(usuario);
        return ResponseEntity.ok(novoUsuario);
    }
}
