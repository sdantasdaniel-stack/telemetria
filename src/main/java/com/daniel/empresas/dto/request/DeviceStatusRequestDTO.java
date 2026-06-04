package com.daniel.empresas.dto.request;

import com.daniel.empresas.model.StatusEnum;
import jakarta.validation.constraints.NotNull;

public record DeviceStatusRequestDTO(
    @NotNull(message = "O status é obrigatório")
    StatusEnum status
) {}