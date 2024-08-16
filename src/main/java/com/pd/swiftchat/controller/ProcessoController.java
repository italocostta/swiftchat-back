package com.pd.swiftchat.controller;

import com.pd.swiftchat.exception.ResourceNotFoundException;
import com.pd.swiftchat.model.Processo;
import com.pd.swiftchat.model.Setor;
import com.pd.swiftchat.model.TipoProcesso;
import com.pd.swiftchat.service.ProcessoService;
import com.pd.swiftchat.service.SetorService;
import com.pd.swiftchat.repository.TipoProcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/processos")
@CrossOrigin(origins = "http://localhost:3000")
public class ProcessoController {

    @Autowired
    private ProcessoService processoService;

    @Autowired
    private TipoProcessoRepository tipoProcessoRepository;

    @Autowired
    private SetorService setorService;

    @GetMapping
    public List<Processo> listarProcessos() {
        return processoService.getAllProcessos();
    }

    @PostMapping
    public ResponseEntity<?> criarProcesso(@RequestBody Processo processo) {
        Optional<TipoProcesso> tipoProcesso = tipoProcessoRepository.findById(processo.getTipoProcesso().getId());
        if (tipoProcesso.isPresent()) {
            processo.setTipoProcesso(tipoProcesso.get());
            try {
                Processo novoProcesso = processoService.createProcesso(processo);
                return ResponseEntity.ok(novoProcesso);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        } else {
            return ResponseEntity.badRequest().body("Tipo de Processo não encontrado");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Processo> obterProcesso(@PathVariable Long id) {
        Optional<Processo> processo = processoService.getProcessoById(id);
        return processo.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Processo> atualizarProcesso(@PathVariable Long id, @RequestBody Processo processoAtualizado) {
        Optional<Processo> processoExistente = processoService.getProcessoById(id);
        if (processoExistente.isPresent()) {
            Processo processo = processoExistente.get();
            processo.setNome(processoAtualizado.getNome());
            processo.setDescricao(processoAtualizado.getDescricao());
            processo.setUsuario(processoAtualizado.getUsuario());
            processo.setCpf(processoAtualizado.getCpf());
            processo.setTipoPessoa(processoAtualizado.getTipoPessoa());
            Optional<TipoProcesso> tipoProcesso = tipoProcessoRepository.findById(processoAtualizado.getTipoProcesso().getId());
            if (tipoProcesso.isPresent()) {
                processo.setTipoProcesso(tipoProcesso.get());
            } else {
                return ResponseEntity.badRequest().body(null);
            }
            return ResponseEntity.ok(processoService.updateProcesso(id, processo));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/setor/{setorId}")
    public ResponseEntity<Processo> moverProcessoParaSetor(@PathVariable Long id, @PathVariable Long setorId) {
        Optional<Processo> processoExistente = processoService.getProcessoById(id);
        Optional<Setor> setorDestino = setorService.getSetorById(setorId);  // Use o setorService para obter o setor
        if (processoExistente.isPresent() && setorDestino.isPresent()) {
            Processo processo = processoExistente.get();
            processo.setSetor(setorDestino.get());
            return ResponseEntity.ok(processoService.updateProcesso(id, processo));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/encaminhar")
    public ResponseEntity<Processo> encaminharParaSetorEspecifico(@PathVariable Long id) {
        Optional<Processo> processoExistente = processoService.getProcessoById(id);
        if (processoExistente.isPresent()) {
            Processo processo = processoExistente.get();
            Setor setorEspecifico = setorService.getSetorPorTipoProcesso(processo.getTipoProcesso());
            processo.setSetor(setorEspecifico);
            return ResponseEntity.ok(processoService.updateProcesso(id, processo));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProcesso(@PathVariable Long id) {
        if (processoService.getProcessoById(id).isPresent()) {
            processoService.deleteProcesso(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
