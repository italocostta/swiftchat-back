package com.pd.swiftchat.service;

import com.pd.swiftchat.model.Processo;
import com.pd.swiftchat.repository.ProcessoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessoServiceTest {

    @Mock
    private ProcessoRepository processoRepository;

    @InjectMocks
    private ProcessoService processoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateProcessoWithExistingUser() {
        // Configurando o mock para simular um processo existente
        Processo processoExistente = new Processo();
        processoExistente.setUsuario("usuario_teste");

        when(processoRepository.findFirstByUsuario("usuario_teste")).thenReturn(Optional.of(processoExistente));

        // Verificando se a exceção é lançada ao tentar criar um processo com o mesmo usuário
        Processo novoProcesso = new Processo();
        novoProcesso.setUsuario("usuario_teste");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            processoService.createProcesso(novoProcesso);
        });

        assertEquals("O usuário já possui um processo. Aguarde o deferimento ou indeferimento do mesmo.", exception.getMessage());
    }

    @Test
    void testCreateProcessoWithNewUser() {
        // Configurando o mock para simular que nenhum processo existe para o usuário
        when(processoRepository.findFirstByUsuario("usuario_novo")).thenReturn(Optional.empty());

        Processo novoProcesso = new Processo();
        novoProcesso.setUsuario("usuario_novo");

        when(processoRepository.save(novoProcesso)).thenReturn(novoProcesso);

        Processo processoCriado = processoService.createProcesso(novoProcesso);

        assertNotNull(processoCriado);
        assertEquals("usuario_novo", processoCriado.getUsuario());
        verify(processoRepository, times(1)).save(novoProcesso);
    }
}
