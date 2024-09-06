package com.pd.swiftchat.service;

import com.pd.swiftchat.exception.SetorJaExisteException;
import com.pd.swiftchat.model.Setor;
import com.pd.swiftchat.model.TipoProcesso;
import com.pd.swiftchat.repository.SetorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SetorService {

    @Autowired
    private SetorRepository setorRepository;

    public List<Setor> getAllSetores() {
        return setorRepository.findAll();
    }

    public Optional<Setor> getSetorById(Long id) {
        return setorRepository.findById(id);
    }

    public Optional<Setor> getSetorByNome(String nome) {
        return setorRepository.findByNome(nome);
    }

    public Setor createSetor(Setor setor) {
        Optional<Setor> existingSetor = setorRepository.findByNome(setor.getNome());
        if (existingSetor.isPresent()) {
            throw new SetorJaExisteException("O setor com o nome fornecido já existe.");
        }
        return setorRepository.save(setor);
    }

    public Setor updateSetor(Long id, Setor setorAtualizado) {
        if (setorRepository.existsById(id)) {
            setorAtualizado.setId(id);
            return setorRepository.save(setorAtualizado);
        }
        return null;
    }

    public void deleteSetor(Long id) {
        setorRepository.deleteById(id);
    }

    public Setor getSetorPorTipoProcesso(TipoProcesso tipoProcesso) {
        // Exemplo de mapeamento, pode ser ajustado conforme a lógica do seu negócio
        if (tipoProcesso.getNome().equalsIgnoreCase("Licença de resíduos sólidos")) {
            return setorRepository.findById(2L).orElseThrow(() -> new RuntimeException("Setor não encontrado"));
        }
        // Adicione outros tipos de mapeamento conforme necessário
        throw new RuntimeException("Setor não encontrado para o tipo de processo: " + tipoProcesso.getNome());
    }
}
