package com.daniel.empresas.dto.request;

public record DeviceImportRequestDTO(
        String nome,
        String identificador,
        Long empresaId
) {
}