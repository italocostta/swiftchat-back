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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getCpfCnpj(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final Usuario usuario = (Usuario) authentication.getPrincipal();
        final String token = jwtTokenUtil.generateToken(usuario.getUsername());

        String userType = usuario.isFuncionario() ? "FUNCIONARIO" : "USUARIO";

        // Aqui retorna o nome completo (nome + sobrenome)
        String nomeCompleto = usuario.getNome() + " " + (usuario.getSobrenome() != null ? usuario.getSobrenome() : "");

        return ResponseEntity.ok(new JwtResponse(token, userType, nomeCompleto));
    }


}
