package com.pd.swiftchat.service;

import com.pd.swiftchat.exception.ResourceNotFoundException;
import com.pd.swiftchat.model.Processo;
import com.pd.swiftchat.model.Setor;
import com.pd.swiftchat.repository.ProcessoRepository;
import com.pd.swiftchat.repository.SetorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ProcessoService {

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private SetorRepository setorRepository;

    public List<Processo> getAllProcessos(UserDetails userDetails) {
        if (isFuncionario(userDetails)) {
            return processoRepository.findAll();
        } else {
            return processoRepository.findByCpf(userDetails.getUsername());
        }
    }


    public Optional<Processo> getProcessoById(Long id) {
        return processoRepository.findById(id);
    }

    public Processo getProcessoById(Long id, UserDetails userDetails) throws ResourceNotFoundException {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado"));

        if (!userDetails.getUsername().equals(processo.getCpf()) && !isFuncionario(userDetails)) {
            throw new AccessDeniedException("Você não tem permissão para acessar este processo.");
        }

        return processo;
    }

    public Processo createProcesso(Processo processo) {
        // Remove a verificação de CPF/CNPJ duplicado
        // Remove a verificação de processo existente para o mesmo usuário

        Optional<Setor> setorIntermediario = setorRepository.findByNome("Setor Intermediario");
        if (setorIntermediario.isPresent()) {
            processo.setSetor(setorIntermediario.get());
        } else {
            throw new RuntimeException("Setor Intermediário não encontrado. Verifique se ele está cadastrado no banco de dados.");
        }

        return processoRepository.save(processo);
    }

    public Processo updateProcesso(Long id, Processo processoAtualizado) {
        if (processoRepository.existsById(id)) {
            processoAtualizado.setId(id);
            return processoRepository.save(processoAtualizado);
        }
        return null;
    }

    public void deleteProcesso(Long id) {
        processoRepository.deleteById(id);
    }

    public void saveFile(Long id, MultipartFile file) throws IOException {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Processo não encontrado"));

        // Código para salvar o arquivo localmente ou em um serviço de armazenamento em nuvem
        // Por exemplo, salvar em um diretório local:
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        file.transferTo(new java.io.File("caminho/para/salvar/arquivos/" + fileName));

        processo.setFileName(fileName);
        processoRepository.save(processo);
    }

    private boolean isFuncionario(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("FUNCIONARIO"));
    }
}
