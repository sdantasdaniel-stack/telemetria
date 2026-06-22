package com.daniel.empresas.dto.response;

import com.daniel.empresas.model.Device;

public record DeviceImportResponseDTO(
        Long id,
        String nome,
        String identificador,
        String empresaNome
) {
    public static DeviceImportResponseDTO fromEntity(Device device) {
        return new DeviceImportResponseDTO(
                device.getId(),
                device.getNome(),
                device.getIdentificador(),
                device.getEmpresa() != null ? device.getEmpresa().getNome() : ""
        );
    }
}