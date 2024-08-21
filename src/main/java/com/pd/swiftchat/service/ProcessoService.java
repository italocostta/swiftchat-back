package com.pd.swiftchat.service;

import com.pd.swiftchat.exception.CpfCnpjJaUtilizadoException;
import com.pd.swiftchat.model.Processo;
import com.pd.swiftchat.model.Setor;
import com.pd.swiftchat.repository.ProcessoRepository;
import com.pd.swiftchat.repository.SetorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProcessoService {

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private SetorRepository setorRepository;  // Adicione essa linha para acessar o SetorRepository

    public List<Processo> getAllProcessos() {
        return processoRepository.findAll();
    }

    public Optional<Processo> getProcessoById(Long id) {
        return processoRepository.findById(id);
    }

    public Processo createProcesso(Processo processo) {
        Optional<Processo> existingProcesso = processoRepository.findByCpf(processo.getCpf());
        if (existingProcesso.isPresent()) {
            throw new CpfCnpjJaUtilizadoException("CPF/CNPJ já utilizado em um processo. Aguarde a conclusão do mesmo.");
        }

        Optional<Processo> existingUsuarioProcesso = processoRepository.findFirstByUsuario(processo.getUsuario());
        if (existingUsuarioProcesso.isPresent()) {
            throw new RuntimeException("O usuário já possui um processo. Aguarde o deferimento ou indeferimento do mesmo.");
        }

        // Busca pelo setor "Setor Intermediário" no banco de dados
        Optional<Setor> setorIntermediario = setorRepository.findByNome("Setor Intermediário");
        if (setorIntermediario.isPresent()) {
            processo.setSetor(setorIntermediario.get());  // Atribui o setor intermediário ao processo
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
}
