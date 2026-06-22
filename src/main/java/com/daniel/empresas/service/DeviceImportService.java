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

    // -------------------------------------------------------------------------
    // Cadastro de um único device (usado pelo card de serial — lógica reutilizável)
    // -------------------------------------------------------------------------

    /**
     * Cadastra um único device a partir de um DTO.
     * Lança IllegalArgumentException se:
     *   - algum campo obrigatório estiver vazio
     *   - o identificador já existir
     *   - a empresa não existir
     */

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

    // -------------------------------------------------------------------------
    // Importação via arquivo (CSV, TXT ou JSON)
    // -------------------------------------------------------------------------

    /**
     * Processa o arquivo recebido e delega para o parser adequado conforme extensão.
     */
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

    // -------------------------------------------------------------------------
    // Parsers internos
    // -------------------------------------------------------------------------

    private ResultadoImportacao importarCsvOuTxt(InputStream inputStream) throws IOException {
        List<String> erros = new ArrayList<>();
        int totalLinhas = 0;
        int sucesso = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String linha;
            boolean primeiraLinha = true;

            while ((linha = reader.readLine()) != null) {
                if (linha.isBlank()) continue;

                // pula cabeçalho se a primeira linha contiver "nome" ou "identificador"
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

                String nome = colunas[0].trim();
                String identificador = colunas[1].trim();
                String empresaIdRaw = colunas[2].trim();

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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // DTO de resultado
    // -------------------------------------------------------------------------

    public record ResultadoImportacao(int totalLinhas, int sucesso, List<String> erros) {
        public int getFalhas() {
            return erros.size();
        }
    }
}