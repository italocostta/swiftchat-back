package com.pd.swiftchat.service;

import com.pd.swiftchat.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UsuarioCacheService {

    private static final String USUARIO_CACHE_KEY = "usuariosTotal";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método para buscar a contagem de usuários
    public Long getUsuariosCount() {
        // Invalidar o cache antes de fazer a consulta
        redisTemplate.delete(USUARIO_CACHE_KEY);

        // Recuperar o valor do cache
        String countStr = (String) redisTemplate.opsForValue().get(USUARIO_CACHE_KEY);
        Long count = null;

        if (countStr != null) {
            try {
                count = Long.parseLong(countStr);
            } catch (NumberFormatException e) {
                System.err.println("Erro ao converter o valor do cache para Long.");
            }
        }

        if (count == null) {
            // Se não houver cache, busque do banco de dados
            count = usuarioRepository.countUsuarios(); // Usando a consulta filtrada
            redisTemplate.opsForValue().set(USUARIO_CACHE_KEY, count.toString());  // Armazena como String
        }

        return count;
    }




    // Método para limpar o cache quando um novo usuário for adicionado
    public void invalidateUsuarioCache() {
        redisTemplate.delete(USUARIO_CACHE_KEY);
    }
}


