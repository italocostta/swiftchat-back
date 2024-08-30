package com.pd.swiftchat.controller;

import com.pd.swiftchat.dto.ProcessoDTO;
import com.pd.swiftchat.exception.ResourceNotFoundException;
import com.pd.swiftchat.model.Processo;
import com.pd.swiftchat.model.Setor;
import com.pd.swiftchat.model.TipoProcesso;
import com.pd.swiftchat.model.Usuario;
import com.pd.swiftchat.repository.UsuarioRepository;
import com.pd.swiftchat.service.ProcessoService;
import com.pd.swiftchat.service.SetorService;
import com.pd.swiftchat.repository.TipoProcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/processos")
@CrossOrigin(origins = "http://localhost:3000")
public class ProcessoController {

    @Autowired
    private ProcessoService processoService;

    @Autowired
    private TipoProcessoRepository tipoProcessoRepository;

    @Autowired
    private SetorService setorService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Secured("USUARIO")
    @GetMapping
    public ResponseEntity<List<ProcessoDTO>> listarProcessos(@AuthenticationPrincipal UserDetails userDetails) {
        List<Processo> processos = processoService.getAllProcessos(userDetails);

        // Converte a lista de Processos em uma lista de ProcessosDTO
        List<ProcessoDTO> processosDTO = processos.stream().map(processo -> {
            ProcessoDTO dto = new ProcessoDTO();
            dto.setId(processo.getId());
            dto.setNome(processo.getNome());
            dto.setDescricao(processo.getDescricao());
            dto.setNumeroProcesso(processo.getNumeroProcesso());
            dto.setCpf(processo.getCpf());
            dto.setTipoPessoa(processo.getTipoPessoa());
            dto.setTipoProcesso(processo.getTipoProcesso());
            dto.setSetor(processo.getSetor());
            dto.setUsuarioNome(processo.getUsuario());
            return dto;
        }).toList();

        return ResponseEntity.ok(processosDTO);
    }

    @Secured("USUARIO")
    @PostMapping
    public ResponseEntity<ProcessoDTO> criarProcesso(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Processo processo) {
        System.out.println("Roles do usuário autenticado: " + userDetails.getAuthorities());

        // Obtém o usuário autenticado
        String cpfOrCnpj = userDetails.getUsername();
        Optional<Usuario> usuarioOpt;

        if (cpfOrCnpj.length() == 11) {  // Se for CPF (11 dígitos)
            usuarioOpt = usuarioRepository.findByCpf(cpfOrCnpj);
        } else if (cpfOrCnpj.length() == 14) {  // Se for CNPJ (14 dígitos)
            usuarioOpt = usuarioRepository.findByCnpj(cpfOrCnpj);
        } else {
            return ResponseEntity.badRequest().body(null);
        }

        Usuario usuario = usuarioOpt.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Associa automaticamente o usuário autenticado ao processo
        processo.setUsuario(usuario.getNome());
        processo.setCpf(usuario.getCpf());
        processo.setTipoPessoa(usuario.getTipoUsuario() == 1 ? "FISICA" : "JURIDICA");

        // Atribui o processo ao setor intermediário automaticamente
        Optional<Setor> setorIntermediario = setorService.getSetorByNome("Setor Intermediario");
        if (setorIntermediario.isPresent()) {
            processo.setSetor(setorIntermediario.get());
        } else {
            return ResponseEntity.badRequest().body(null);
        }

        Optional<TipoProcesso> tipoProcesso = tipoProcessoRepository.findById(processo.getTipoProcesso().getId());
        if (tipoProcesso.isPresent()) {
            processo.setTipoProcesso(tipoProcesso.get());
            try {
                Processo novoProcesso = processoService.createProcesso(processo);

                // Converte o Processo em ProcessoDTO para retornar uma resposta apropriada
                ProcessoDTO processoDTO = new ProcessoDTO();
                processoDTO.setId(novoProcesso.getId());
                processoDTO.setNome(novoProcesso.getNome());
                processoDTO.setDescricao(novoProcesso.getDescricao());
                processoDTO.setNumeroProcesso(novoProcesso.getNumeroProcesso());
                processoDTO.setCpf(novoProcesso.getCpf());
                processoDTO.setTipoPessoa(novoProcesso.getTipoPessoa());
                processoDTO.setTipoProcesso(novoProcesso.getTipoProcesso());
                processoDTO.setSetor(novoProcesso.getSetor());
                processoDTO.setUsuarioNome(novoProcesso.getUsuario());

                return ResponseEntity.ok(processoDTO);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(null);
            }
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }



    @Secured("USUARIO")
    @GetMapping("/{id}")
    public ResponseEntity<Processo> obterProcesso(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Processo processo = processoService.getProcessoById(id, userDetails);
            return ResponseEntity.ok(processo);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Secured("USUARIO")
    @PutMapping("/{id}")
    public ResponseEntity<Processo> atualizarProcesso(@PathVariable Long id, @RequestBody Processo processoAtualizado) {
        Optional<Processo> processoExistente = processoService.getProcessoById(id);
        if (processoExistente.isPresent()) {
            Processo processo = processoExistente.get();
            processo.setNome(processoAtualizado.getNome());
            processo.setDescricao(processoAtualizado.getDescricao());
            processo.setTipoPessoa(processoAtualizado.getTipoPessoa());
            if (processoAtualizado.getTipoProcesso() != null && processoAtualizado.getTipoProcesso().getId() != null) {
                Optional<TipoProcesso> tipoProcessoOpt = tipoProcessoRepository.findById(processoAtualizado.getTipoProcesso().getId());
                if (tipoProcessoOpt.isPresent()) {
                    processo.setTipoProcesso(tipoProcessoOpt.get());
                } else {
                    return ResponseEntity.badRequest().body(null);
                }
            }
            return ResponseEntity.ok(processoService.updateProcesso(id, processo));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Secured("FUNCIONARIO")
    @PutMapping("/{id}/setor/{setorId}")
    public ResponseEntity<Processo> moverProcessoParaSetor(@PathVariable Long id, @PathVariable Long setorId) {
        Optional<Processo> processoExistente = processoService.getProcessoById(id);
        Optional<Setor> setorDestino = setorService.getSetorById(setorId);
        if (processoExistente.isPresent() && setorDestino.isPresent()) {
            Processo processo = processoExistente.get();
            processo.setSetor(setorDestino.get());
            return ResponseEntity.ok(processoService.updateProcesso(id, processo));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Secured("FUNCIONARIO")
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

    @Secured("FUNCIONARIO")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarProcesso(@PathVariable Long id) {
        if (processoService.getProcessoById(id).isPresent()) {
            processoService.deleteProcesso(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Secured("USUARIO")
    @PostMapping("/{id}/upload")
    public ResponseEntity<?> uploadArquivo(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            processoService.saveFile(id, file);
            return ResponseEntity.ok("Arquivo carregado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Falha no upload do arquivo.");
        }
    }
}
