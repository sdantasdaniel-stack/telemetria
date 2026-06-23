package com.daniel.empresas.service;

import com.daniel.empresas.dto.request.DeviceImportRequestDTO;
import com.daniel.empresas.dto.response.DeviceImportResponseDTO;
import com.daniel.empresas.model.Device;
import com.daniel.empresas.model.Empresa;
import com.daniel.empresas.model.StatusEnum;
import com.daniel.empresas.repository.DeviceRepository;
import com.daniel.empresas.repository.EmpresaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeviceImportService {

    private final DeviceRepository deviceRepository;
    private final EmpresaRepository empresaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeviceImportService(DeviceRepository deviceRepository,
                               EmpresaRepository empresaRepository) {
        this.deviceRepository = deviceRepository;
        this.empresaRepository = empresaRepository;
    }

    public List<DeviceImportResponseDTO> listarTodos() {
        return deviceRepository.findAll().stream()
                .map(DeviceImportResponseDTO::fromEntity)
                .toList();
    }

    public DeviceImportResponseDTO cadastrar(DeviceImportRequestDTO dto) {
        validarCampos(dto);

        if (deviceRepository.findByIdentificador(dto.identificador()).isPresent()) {
            throw new IllegalArgumentException(
                    "Identificador \"" + dto.identificador() + "\" já está cadastrado");
        }

        Empresa empresa = empresaRepository.findById(dto.empresaId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Empresa com ID " + dto.empresaId() + " não encontrada"));

        Device device = new Device();
        device.setNome(dto.nome());
        device.setIdentificador(dto.identificador());
        device.setEmpresa(empresa);
        device.setStatus(StatusEnum.OFFLINE);
        device.setAtivo(true);

        return DeviceImportResponseDTO.fromEntity(deviceRepository.save(device));
    }

    public ResultadoImportacao importarArquivo(InputStream inputStream,
                                               String nomeArquivo) throws IOException {
        String extensao = extensao(nomeArquivo);
        return switch (extensao) {
            case "json" -> importarJson(inputStream);
            case "csv", "txt" -> importarCsvOuTxt(inputStream);
            default -> throw new IllegalArgumentException(
                    "Formato não suportado: " + extensao + ". Use JSON, CSV ou TXT.");
        };
    }

    private ResultadoImportacao importarCsvOuTxt(InputStream inputStream) throws IOException {
        List<String> erros = new ArrayList<>();
        int totalLinhas = 0;
        int sucesso = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String linha;
            boolean primeiraLinha = true;

            while ((linha = reader.readLine()) != null) {
                // remove BOM (byte order mark) que o Windows/Excel adiciona no início do arquivo
                if (primeiraLinha) {
                    linha = linha.replace("\uFEFF", "");
                }

                if (linha.isBlank()) continue;

                // pula cabeçalho se a primeira linha contiver "nome"
                if (primeiraLinha && linha.toLowerCase().contains("nome")) {
                    primeiraLinha = false;
                    continue;
                }
                primeiraLinha = false;
                totalLinhas++;

                String[] colunas = linha.split(",");
                if (colunas.length < 3) {
                    erros.add("Linha " + totalLinhas + ": esperado 3 colunas (nome,identificador,empresaId), encontrado " + colunas.length);
                    continue;
                }

                // limpa aspas e espaços de cada coluna — cobre arquivos gerados pelo Excel
                String nome = limpar(colunas[0]);
                String identificador = limpar(colunas[1]);
                String empresaIdRaw = limpar(colunas[2]);

                try {
                    Long empresaId = Long.parseLong(empresaIdRaw);
                    cadastrar(new DeviceImportRequestDTO(nome, identificador, empresaId));
                    sucesso++;
                } catch (NumberFormatException e) {
                    erros.add("Linha " + totalLinhas + ": empresaId \"" + empresaIdRaw + "\" não é um número válido");
                } catch (IllegalArgumentException e) {
                    erros.add("Linha " + totalLinhas + " (" + identificador + "): " + e.getMessage());
                }
            }
        }

        return new ResultadoImportacao(totalLinhas, sucesso, erros);
    }

    private ResultadoImportacao importarJson(InputStream inputStream) throws IOException {
        List<DeviceImportRequestDTO> lista = objectMapper.readValue(
                inputStream, new TypeReference<List<DeviceImportRequestDTO>>() {});

        List<String> erros = new ArrayList<>();
        int sucesso = 0;
        int totalLinhas = lista.size();

        for (int i = 0; i < lista.size(); i++) {
            DeviceImportRequestDTO dto = lista.get(i);
            try {
                cadastrar(dto);
                sucesso++;
            } catch (IllegalArgumentException e) {
                erros.add("Item " + (i + 1) + " (" + dto.identificador() + "): " + e.getMessage());
            }
        }

        return new ResultadoImportacao(totalLinhas, sucesso, erros);
    }

    private void validarCampos(DeviceImportRequestDTO dto) {
        if (dto.nome() == null || dto.nome().isBlank())
            throw new IllegalArgumentException("O nome é obrigatório");
        if (dto.identificador() == null || dto.identificador().isBlank())
            throw new IllegalArgumentException("O identificador é obrigatório");
        if (dto.empresaId() == null)
            throw new IllegalArgumentException("O ID da empresa é obrigatório");
    }

    private String extensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) return "";
        return nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * Remove aspas duplas, BOM e espaços extras de um valor CSV.
     * Cobre arquivos gerados pelo Excel e pelo Notepad do Windows.
     */
    private String limpar(String valor) {
        if (valor == null) return "";
        return valor.trim()
                .replace("\"", "")
                .replace("\uFEFF", "");
    }

    public record ResultadoImportacao(int totalLinhas, int sucesso, List<String> erros) {
        public int getFalhas() {
            return erros.size();
        }
    }
}