package com.daniel.empresas.controller;

import java.util.List;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.daniel.empresas.dto.request.DeviceRequestDTO;
import com.daniel.empresas.dto.request.DeviceStatusRequestDTO;
import com.daniel.empresas.dto.response.DeviceResponseDTO;
import com.daniel.empresas.exception.EstadoInvalidoException;
import com.daniel.empresas.service.DeviceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.daniel.empresas.model.RoleEnum;
import com.daniel.empresas.model.Usuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

// marca a classe como Controller REST — recebe requisições HTTP e retorna JSON
@RestController

// Essa anotação mapeia URLs de requisições HTTP para classes ou métodos específicos do seu controlador
// prefixo de todas as rotas desse Controller — toda rota começa com /devices
@RequestMapping("/devices")

// gera construtor com todos os campos final para injeção pelo Spring
@RequiredArgsConstructor

public class DeviceController {

    // único campo injetado — o Controller só conhece o Service
    private final DeviceService deviceService;

    // Essa anotação do Spring é usada para mapear requisições HTTP GET para métodos específicos em controladores
    // responde GET /devices — retorna todos os devices — apenas ADMIN pode listar todos (ativos e inativos)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeviceResponseDTO>> listarTodos() {
        // chama o Service que busca e converte todos os devices para DTO
        List<DeviceResponseDTO> devices = deviceService.listarTodos();
        // ResponseEntity.ok() cria uma resposta HTTP com status 200 OK
        // passa a lista de devices como corpo da resposta
        // o Spring converte automaticamente a lista para JSON antes de enviar para o cliente
        return ResponseEntity.ok(devices);
    }

    // responde GET /devices/ativos — retorna apenas devices ativos — apenas USER pode acessar
    // USER não deve ver devices inativos
    @GetMapping("/ativos")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<DeviceResponseDTO>> listarAtivos(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(deviceService.listarAtivosPorUsuario(usuario));
}

    // responde GET /devices/{id} — retorna um device pelo ID — apenas ADMIN
    // @PathVariable extrai o valor {id} da URL e passa para o parâmetro id
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeviceResponseDTO> buscarPorId(@PathVariable Long id) {
        // chama o Service — lança DeviceNaoEncontradoException se não existir
        DeviceResponseDTO device = deviceService.buscarPorId(id);
        return ResponseEntity.ok(device);
    }

    
    // responde PUT /devices/{id} — atualiza um device existente — apenas ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeviceResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid DeviceRequestDTO dto) {
        // chama o Service que valida e atualiza o device
        DeviceResponseDTO deviceAtualizado = deviceService.atualizar(id, dto);
        return ResponseEntity.ok(deviceAtualizado);
    }

    // responde DELETE /devices/{id} — desativa um device — apenas ADMIN
    // retorna Void porque não há dado para devolver
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        // chama o Service que desativa o device — seta ativo = false
        deviceService.desativar(id);
        // O método .build() é necessário porque você está retornando uma resposta sem corpo (Void)
        // retorna status 204 No Content — deu certo mas não há corpo na resposta
        return ResponseEntity.noContent().build();
    }

    // responde DELETE /devices/{id}/deletar — deleta permanentemente um device — apenas ADMIN
    @DeleteMapping("/{id}/deletar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        deviceService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // responde PATCH /devices/{id}/reativar — reativa um device desativado — apenas ADMIN
    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        deviceService.reativar(id);
        return ResponseEntity.noContent().build();
    }
    
    // endpoint novo — USER lista devices de uma empresa específica que ele pertence
    // ADMIN pode listar de qualquer empresa (vê todos incluindo inativos)
    // USER só pode listar de empresa que pertence (vê apenas ativos)
    @GetMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<DeviceResponseDTO>> listarPorEmpresa(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal Usuario usuario) {

        if (usuario.getRole() == RoleEnum.USER) {
            boolean pertence = usuario.getEmpresas().stream()
                    .anyMatch(e -> e.getId().equals(empresaId));
            if (!pertence) {
                throw new AccessDeniedException("Você não pertence a esta empresa");
            }

            // adicione essa verificação — USER não deve ver devices de empresa inativa
            boolean empresaAtiva = usuario.getEmpresas().stream()
                    .anyMatch(e -> e.getId().equals(empresaId) && e.isAtivo());
            if (!empresaAtiva) {
                throw new EstadoInvalidoException("Empresa inativa");
            }

            return ResponseEntity.ok(deviceService.listarAtivosPorEmpresa(empresaId));
        }

        return ResponseEntity.ok(deviceService.listarPorEmpresa(empresaId));
    }
    
    // endpoint chamado pelo device físico para reportar seu status
    // qualquer usuário autenticado pode chamar — o device usa o token do sistema
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> atualizarStatus(
    		@PathVariable Long id,
    		@RequestBody @Valid DeviceStatusRequestDTO dto) {
    	deviceService.atualizarStatus(id, dto.status());
    	return ResponseEntity.noContent().build();
    }
 

 	// validação de pertencimento no cadastrar — USER só cadastra em empresa que pertence
 	@PostMapping
 	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
 	public ResponseEntity<DeviceResponseDTO> cadastrar(
 			@RequestBody @Valid DeviceRequestDTO dto,
 			@AuthenticationPrincipal Usuario usuario) {

 		if (usuario.getRole() == RoleEnum.USER) {
 			boolean pertence = usuario.getEmpresas().stream()
 					.anyMatch(e -> e.getId().equals(dto.empresaId()));
 			if (!pertence) {
 				throw new AccessDeniedException("Você não pertence a esta empresa");
 			}
 		}

 		DeviceResponseDTO deviceSalvo = deviceService.cadastrar(dto);
 		return ResponseEntity.status(HttpStatus.CREATED).body(deviceSalvo);
 	}
}