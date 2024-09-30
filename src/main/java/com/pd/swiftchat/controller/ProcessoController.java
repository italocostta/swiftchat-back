package com.pd.swiftchat.controller;

import com.pd.swiftchat.dto.ProcessoDTO;
import com.pd.swiftchat.exception.ResourceNotFoundException;
import com.pd.swiftchat.model.*;
import com.pd.swiftchat.repository.UsuarioRepository;
import com.pd.swiftchat.service.ProcessoService;
import com.pd.swiftchat.service.SetorService;
import com.pd.swiftchat.repository.TipoProcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/processos")
@CrossOrigin(origins = "http://localhost:3000")
public class ProcessoController {

    private static final String UPLOAD_DIR = "uploads/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // Limite de 5 MB para arquivos

    @Autowired
    private ProcessoService processoService;

    @Autowired
    private TipoProcessoRepository tipoProcessoRepository;

    @Autowired
    private SetorService setorService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Esse método é compartilhado entre usuário comum e funcionário
    @Secured({"USUARIO", "FUNCIONARIO"})
    @GetMapping
    public ResponseEntity<List<ProcessoDTO>> listarProcessos(@AuthenticationPrincipal UserDetails userDetails) {
        List<Processo> processos;

        // Chama o método adequado de acordo com o tipo de usuário
        processos = processoService.getAllProcessosByRole(userDetails);

        // Converte a lista de Processos em uma lista de ProcessosDTO
        List<ProcessoDTO> processosDTO = processos.stream().map(processo -> {
            ProcessoDTO dto = new ProcessoDTO();
            dto.setId(processo.getId());
            dto.setNome(processo.getNome());
            dto.setDescricao(processo.getDescricao());
            dto.setNumeroProcesso(processo.getNumeroProcesso());
            dto.setCpf(processo.getCpf());
            dto.setCnpj(processo.getCnpj());  // Inclui o CNPJ, se existir
            dto.setTipoPessoa(processo.getTipoPessoa());
            dto.setTipoProcesso(processo.getTipoProcesso());
            dto.setSetor(processo.getSetor());
            dto.setUsuarioNome(processo.getUsuario());
            dto.setArquivo(processo.getArquivo());  // Inclui o campo arquivo
            return dto;
        }).toList();

        return ResponseEntity.ok(processosDTO);
    }

    @Secured("USUARIO")
    @PostMapping
    public ResponseEntity<ProcessoDTO> criarProcesso(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("processo") Processo processo,
            @RequestPart(value = "arquivo", required = false) MultipartFile arquivo
    ) throws IOException {
        System.out.println("Processo recebido: " + processo.getNome());
        if (arquivo != null) {
            System.out.println("Arquivo recebido: " + arquivo.getOriginalFilename());
        } else {
            System.out.println("Nenhum arquivo recebido");
        }

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

        // Ajusta o CPF ou CNPJ dependendo do tipo de pessoa
        if (usuario.getCpf() != null) {
            processo.setCpf(usuario.getCpf());
            processo.setTipoPessoa("FISICA");
        } else if (usuario.getCnpj() != null) {
            processo.setCpf(null);  // Certifique-se de que o CPF seja nulo
            processo.setCnpj(usuario.getCnpj());  // Ajusta o CNPJ
            processo.setTipoPessoa("JURIDICA");
        }

        // Atribui o processo ao setor intermediário automaticamente
        Optional<Setor> setorIntermediario = setorService.getSetorByNome("Setor Intermediario");
        if (setorIntermediario.isPresent()) {
            processo.setSetor(setorIntermediario.get());
            // Atribui o status do processo como RECEBIDO
            processo.setStatusProcesso("RECEBIDO");
            System.out.println(processo.getStatusProcesso());
        } else {
            return ResponseEntity.badRequest().body(null);
        }

        Optional<TipoProcesso> tipoProcesso = tipoProcessoRepository.findById(processo.getTipoProcesso().getId());
        if (tipoProcesso.isPresent()) {
            processo.setTipoProcesso(tipoProcesso.get());

            // Verifica se um arquivo foi enviado e o salva
            if (arquivo != null && !arquivo.isEmpty()) {
                String nomeArquivo = salvarArquivo(arquivo);
                processo.setArquivo(nomeArquivo);  // Aqui, você pode armazenar o nome do arquivo no processo
            }

            try {
                Processo novoProcesso = processoService.createProcesso(processo);

                // Converte o Processo em ProcessoDTO para retornar uma resposta apropriada
                ProcessoDTO processoDTO = new ProcessoDTO();
                processoDTO.setId(novoProcesso.getId());
                processoDTO.setNome(novoProcesso.getNome());
                processoDTO.setDescricao(novoProcesso.getDescricao());
                processoDTO.setNumeroProcesso(novoProcesso.getNumeroProcesso());
                processoDTO.setCpf(novoProcesso.getCpf());
                processoDTO.setCnpj(novoProcesso.getCnpj());  // Inclui o CNPJ no DTO
                processoDTO.setTipoPessoa(novoProcesso.getTipoPessoa());
                processoDTO.setTipoProcesso(novoProcesso.getTipoProcesso());
                processoDTO.setSetor(novoProcesso.getSetor());
                processoDTO.setUsuarioNome(novoProcesso.getUsuario());
                processoDTO.setArquivo(novoProcesso.getArquivo()); // Inclui o arquivo no DTO

                return ResponseEntity.ok(processoDTO);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(null);
            }
        } else {
            return ResponseEntity.badRequest().body(null);
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

    private String salvarArquivo(MultipartFile arquivo) throws IOException {
        // Defina o diretório onde os arquivos serão salvos
        String diretorioUploads = "uploads";  // Apenas "uploads", sem concatenar várias vezes

        // Crie o diretório se ele não existir
        Path caminhoDiretorioUploads = Paths.get(diretorioUploads);
        if (!Files.exists(caminhoDiretorioUploads)) {
            Files.createDirectories(caminhoDiretorioUploads);
        }

        // Salve o arquivo no diretório de uploads
        Path caminhoArquivo = caminhoDiretorioUploads.resolve(Objects.requireNonNull(arquivo.getOriginalFilename()));
        Files.copy(arquivo.getInputStream(), caminhoArquivo, StandardCopyOption.REPLACE_EXISTING);

        // Retorne o nome do arquivo salvo
        return arquivo.getOriginalFilename();  // Somente o nome do arquivo, sem o caminho completo
    }


    @Secured({"USUARIO", "FUNCIONARIO"})
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadArquivo(@PathVariable Long id) {
        Optional<Processo> processoOpt = processoService.getProcessoById(id);
        if (processoOpt.isPresent()) {
            Processo processo = processoOpt.get();
            String nomeArquivo = processo.getArquivo();  // Usando o campo arquivo

            try {
                // Ajustar o caminho para não duplicar o diretório "uploads"
                Path filePath = Paths.get("uploads").resolve(nomeArquivo).toAbsolutePath().normalize();
                System.out.println("Caminho completo do arquivo: " + filePath.toString());

                Resource resource = new UrlResource(filePath.toUri());

                if (resource.exists() && resource.isReadable()) {
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                            .body(resource);
                } else {
                    throw new RuntimeException("Arquivo não encontrado ou não é legível.");
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException("Erro ao baixar o arquivo: " + e.getMessage());
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
