package com.pd.swiftchat.service;

import com.pd.swiftchat.model.Usuario;
import com.pd.swiftchat.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String cpfOrCnpj) throws UsernameNotFoundException {
        // Tenta buscar o usuário por CPF ou CNPJ
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCpf(cpfOrCnpj);

        if (!usuarioOpt.isPresent()) {
            usuarioOpt = usuarioRepository.findByCnpj(cpfOrCnpj);
        }

        Usuario usuario = usuarioOpt.orElseThrow(() ->
                new UsernameNotFoundException("Usuário não encontrado com CPF/CNPJ: " + cpfOrCnpj));

        return usuario;
    }
}
