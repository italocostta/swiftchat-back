package com.pd.swiftchat.controller;

import com.pd.swiftchat.exception.CpfCnpjJaUtilizadoException;
import com.pd.swiftchat.model.Setor;
import com.pd.swiftchat.model.Usuario;
import com.pd.swiftchat.repository.SetorRepository;
import com.pd.swiftchat.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SetorRepository setorRepository;

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

        // Verifica o tipo de usuário e faz as validações necessárias
        if (usuario.getTipoUsuario() == 2) {  // Funcionário
            // Funcionário só pode ser Pessoa Física (com CPF)
            if (usuario.getCpf() == null) {
                throw new IllegalArgumentException("Funcionário deve ter CPF.");
            }
            if (usuario.getMatricula() == null || usuario.getMatricula().length() != 11) {
                throw new IllegalArgumentException("Funcionário deve ter uma matrícula válida de 11 dígitos.");
            }
            usuario.setTipoPessoa("FISICA");  // Funcionário sempre será Pessoa Física

            // Associando o funcionário ao setor
            if (usuario.getSetor() == null || usuario.getSetor().getId() == null) {
                throw new IllegalArgumentException("Funcionário deve ser associado a um setor.");
            }
            Optional<Setor> setorOpt = setorRepository.findById(usuario.getSetor().getId());
            if (setorOpt.isPresent()) {
                usuario.setSetor(setorOpt.get());
            } else {
                throw new IllegalArgumentException("Setor inválido.");
            }

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
        if (!validarNome(usuario.getNome()) ||
                (usuario.getTipoPessoa() != null && usuario.getTipoPessoa().equals("FISICA") && !validarNome(usuario.getSobrenome()))) {
            throw new IllegalArgumentException("Nome e sobrenome devem conter apenas letras e começar com letra maiúscula.");
        }

        // Verificação se já existe um usuário com o mesmo CPF ou CNPJ
        Optional<Usuario> existingUsuario;
        if (usuario.getCpf() != null) {
            existingUsuario = usuarioService.findByCpf(usuario.getCpf());
        } else {
            existingUsuario = usuarioService.findByCnpj(usuario.getCnpj());
        }

        if (existingUsuario.isPresent()) {
            throw new CpfCnpjJaUtilizadoException("Usuário com o mesmo CPF ou CNPJ já existe.");
        }

        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            throw new IllegalArgumentException("A senha não pode ser nula ou vazia.");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        Usuario novoUsuario = usuarioService.salvar(usuario);
        return ResponseEntity.ok(novoUsuario);
    }

    private boolean validarNome(String nome) {
        return nome.matches("^[A-Z][a-zA-ZÀ-ÿ\\s]+$");  // Nome deve começar com letra maiúscula e conter apenas letras
    }
}
