package com.pd.swiftchat.controller;

import com.pd.swiftchat.model.Processo;
import com.pd.swiftchat.service.ProcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/processos")
public class ProcessoController {

    @Autowired
    private ProcessoService processoService;

    @GetMapping
    public List<Processo> getAllProcessos(){
        return processoService.getAllProcessos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Processo> getProcessoById(@PathVariable int id){
        Optional<Processo> processo = processoService.getProcessoById(id);
        return processo.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Processo> createProcesso(@RequestBody Processo processo){
        Processo createdProcesso = processoService.createProcesso(processo);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProcesso);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Processo> updateProcesso(@PathVariable int id, @RequestBody Processo processo){
        Processo updatedProcesso = processoService.updateProcesso((long)id, processo);
        return updatedProcesso != null ? ResponseEntity.ok(updatedProcesso) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProcesso(@PathVariable int id){
        processoService.deleteProcesso(id);
        return ResponseEntity.noContent().build();
    }
}
