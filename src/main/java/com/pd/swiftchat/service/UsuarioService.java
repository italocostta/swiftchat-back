package com.pd.swiftchat.service;

import com.pd.swiftchat.dto.UsuarioUpdateDTO;
import com.pd.swiftchat.model.Usuario;
import com.pd.swiftchat.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario salvar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> findByCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf);
    }

    public Optional<Usuario> findByCnpj(String cnpj) {
        return usuarioRepository.findByCnpj(cnpj);
    }

    public Usuario updateUsuario(Long id, UsuarioUpdateDTO usuarioAtualizado) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            Usuario usuarioAtual = usuario.get();
            usuarioAtual.setNome(usuarioAtualizado.getNome());
            usuarioAtual.setSobrenome(usuarioAtualizado.getSobrenome());
            return usuarioRepository.save(usuarioAtual);
        }
        return null;
    }
}
