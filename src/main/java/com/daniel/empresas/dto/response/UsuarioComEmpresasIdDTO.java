package com.daniel.empresas.dto.response;

import java.util.List;
import com.daniel.empresas.model.RoleEnum;

public record UsuarioComEmpresasIdDTO(
        Long id,
        String nome,
        String email,
        RoleEnum role,
        boolean ativo,
        List<Long> empresasIds
) {}