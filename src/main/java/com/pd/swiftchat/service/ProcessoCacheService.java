package com.pd.swiftchat.service;
import com.pd.swiftchat.repository.ProcessoRepository;
import com.pd.swiftchat.repository.TipoProcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProcessoCacheService {

    private static final String PROCESSO_MAIS_CRIADO_CACHE_KEY = "processoMaisCriado";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private TipoProcessoRepository tipoProcessoRepository;

    // Método para buscar o tipo de processo mais criado
    public String getTipoProcessoMaisCriado() {
        String processoMaisCriado = (String) redisTemplate.opsForValue().get(PROCESSO_MAIS_CRIADO_CACHE_KEY);

        if (processoMaisCriado == null) {
            // Busca o ID do tipo de processo mais criado
            Long tipoProcessoId = processoRepository.findProcessoMaisCriado();

            // Busca o nome do tipo de processo a partir do ID
            if (tipoProcessoId != null) {
                processoMaisCriado = tipoProcessoRepository.findById(tipoProcessoId)
                        .map(tipo -> tipo.getNome()) // Supondo que você tenha o método getNome() na entidade TipoProcesso
                        .orElse("Desconhecido");
            } else {
                processoMaisCriado = "Nenhum processo encontrado";
            }

            // Armazena no cache
            redisTemplate.opsForValue().set(PROCESSO_MAIS_CRIADO_CACHE_KEY, processoMaisCriado);
        }

        return processoMaisCriado;
    }

    // Método para invalidar o cache quando necessário
    public void invalidateProcessoCache() {
        redisTemplate.delete(PROCESSO_MAIS_CRIADO_CACHE_KEY);
    }
}
