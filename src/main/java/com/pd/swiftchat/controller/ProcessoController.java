package com.pd.swiftchat.controller;

import com.pd.swiftchat.exception.ResourceNotFoundException;
import com.pd.swiftchat.model.Processo;
import com.pd.swiftchat.model.Setor;
import com.pd.swiftchat.model.TipoProcesso;
import com.pd.swiftchat.repository.ProcessoRepository;
import com.pd.swiftchat.repository.SetorRepository;
import com.pd.swiftchat.repository.TipoProcessoRepository;
import com.pd.swiftchat.service.SetorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/processos")
public class ProcessoController {

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private TipoProcessoRepository tipoProcessoRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private SetorService setorService;

    @GetMapping
    public List<Processo> listarProcessos() {
        return processoRepository.findAll();
    }

    @PostMapping
    public Processo criarProcesso(@RequestBody Processo processo) {
        Optional<TipoProcesso> tipoProcesso = tipoProcessoRepository.findById(processo.getTipoProcesso().getId());
        if (tipoProcesso.isPresent()) {
            processo.setTipoProcesso(tipoProcesso.get());
            // Set default intermediate sector
            Optional<Setor> setorIntermediario = setorRepository.findById(1L); // Supondo que o setor intermediário tem ID 1
            setorIntermediario.ifPresent(processo::setSetor);
            return processoRepository.save(processo);
        } else {
            throw new ResourceNotFoundException("Tipo de Processo não encontrado");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Processo> obterProcesso(@PathVariable Long id) {
        Optional<Processo> processo = processoRepository.findById(id);
        if (processo.isPresent()) {
            return ResponseEntity.ok(processo.get());
        } else {
            throw new ResourceNotFoundException("Processo não encontrado com id: " + id);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Processo> atualizarProcesso(@PathVariable Long id, @RequestBody Processo processoAtualizado) {
        Optional<Processo> processoExistente = processoRepository.findById(id);
        if (processoExistente.isPresent()) {
            Processo processo = processoExistente.get();
            processo.setNome(processoAtualizado.getNome());
            processo.setDescricao(processoAtualizado.getDescricao());
            processo.setUsuario(processoAtualizado.getUsuario());
            Optional<TipoProcesso> tipoProcesso = tipoProcessoRepository.findById(processoAtualizado.getTipoProcesso().getId());
            if (tipoProcesso.isPresent()) {
                processo.setTipoProcesso(tipoProcesso.get());
            } else {
                throw new ResourceNotFoundException("Tipo de Processo não encontrado");
            }
            return ResponseEntity.ok(processoRepository.save(processo));
        } else {
            throw new ResourceNotFoundException("Processo não encontrado com id: " + id);
        }
    }

    @PutMapping("/{id}/setor/{setorId}")
    public ResponseEntity<Processo> moverProcessoParaSetor(@PathVariable Long id, @PathVariable Long setorId) {
        Optional<Processo> processoExistente = processoRepository.findById(id);
        Optional<Setor> setorDestino = setorRepository.findById(setorId);
        if (processoExistente.isPresent() && setorDestino.isPresent()) {
            Processo processo = processoExistente.get();
            processo.setSetor(setorDestino.get());
            return ResponseEntity.ok(processoRepository.save(processo));
        } else {
            throw new ResourceNotFoundException("Processo ou Setor não encontrado");
        }
    }

    @PutMapping("/{id}/encaminhar")
    public ResponseEntity<Processo> encaminharParaSetorEspecifico(@PathVariable Long id) {
        Optional<Processo> processoExistente = processoRepository.findById(id);
        if (processoExistente.isPresent()) {
            Processo processo = processoExistente.get();
            Setor setorEspecifico = setorService.getSetorPorTipoProcesso(processo.getTipoProcesso());
            processo.setSetor(setorEspecifico);
            return ResponseEntity.ok(processoRepository.save(processo));
        } else {
            throw new ResourceNotFoundException("Processo não encontrado com id: " + id);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProcesso(@PathVariable Long id) {
        if (processoRepository.existsById(id)) {
            processoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            throw new ResourceNotFoundException("Processo não encontrado com id: " + id);
        }
    }
}
