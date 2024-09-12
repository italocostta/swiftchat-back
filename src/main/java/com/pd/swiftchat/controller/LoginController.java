package com.pd.swiftchat.controller;

import com.pd.swiftchat.dto.JwtResponse;
import com.pd.swiftchat.dto.LoginRequest;
import com.pd.swiftchat.model.Usuario;
import com.pd.swiftchat.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        System.out.println("Tentativa de login com CPF/CNPJ: " + loginRequest.getCpfCnpj());
        System.out.println("Senha fornecida: " + loginRequest.getPassword());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getCpfCnpj(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final Usuario usuario = (Usuario) authentication.getPrincipal();  // Aqui retorna o objeto Usuario
        final String token = jwtTokenUtil.generateToken(usuario.getUsername());

        // Verifica o tipo de usuário (FUNCIONARIO ou USUARIO)
        String userType = usuario.isFuncionario() ? "FUNCIONARIO" : "USUARIO";

        // Retorna a resposta com o token, o tipo de usuário e o nome do usuário
        return ResponseEntity.ok(new JwtResponse(token, userType, usuario.getNome()));
    }
}
