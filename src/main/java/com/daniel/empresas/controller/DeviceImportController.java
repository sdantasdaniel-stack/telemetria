package com.daniel.empresas.controller;

import com.daniel.empresas.dto.request.DeviceImportRequestDTO;
import com.daniel.empresas.dto.response.DeviceImportResponseDTO;
import com.daniel.empresas.service.DeviceImportService;
import com.daniel.empresas.service.DeviceImportService.ResultadoImportacao;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

// controller dedicado à importação de devices via API REST
// separado do DeviceController para não misturar responsabilidades
@RestController
@RequestMapping("/devices/importar")
@RequiredArgsConstructor
public class DeviceImportController {

    private final DeviceImportService deviceImportService;

    // GET /devices/importar — lista todos os devices cadastrados
    // útil para conferir o resultado após uma importação no Postman
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeviceImportResponseDTO>> listarTodos() {
        return ResponseEntity.ok(deviceImportService.listarTodos());
    }

    // POST /devices/importar/serial — importa um único device pelo serial
    // body: { "nome": "Device X", "identificador": "DEV-001", "empresaId": 1 }
    @PostMapping("/serial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> importarPorSerial(
            @RequestBody @Valid DeviceImportRequestDTO dto) {
        try {
            DeviceImportResponseDTO response = deviceImportService.cadastrar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", response.id(),
                "nome", response.nome(),
                "identificador", response.identificador(),
                "empresaNome", response.empresaNome()
        ));
    } catch (IllegalArgumentException e) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("erro", e.getMessage()));
    }
    }

    // POST /devices/importar/arquivo — importa devices via arquivo JSON, CSV ou TXT
    // usar form-data no Postman: campo "arquivo" do tipo File
    @PostMapping("/arquivo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> importarArquivo(
            @RequestParam("arquivo") MultipartFile arquivo) {

        if (arquivo.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("erro", "Arquivo não pode estar vazio"));
        }

        try {
            ResultadoImportacao resultado = deviceImportService.importarArquivo(
                    arquivo.getInputStream(),
                    arquivo.getOriginalFilename());

            // monta resposta com resumo da importação
            Map<String, Object> resposta = Map.of(
                    "totalLinhas", resultado.totalLinhas(),
                    "sucesso", resultado.sucesso(),
                    "falhas", resultado.getFalhas(),
                    "erros", resultado.erros()
            );

            // 207 Multi-Status — indica que parte foi processada com sucesso e parte falhou
            HttpStatus status = resultado.getFalhas() == 0
                    ? HttpStatus.OK
                    : HttpStatus.MULTI_STATUS;

            return ResponseEntity.status(status).body(resposta);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("erro", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao ler o arquivo: " + e.getMessage()));
        }
    }
}