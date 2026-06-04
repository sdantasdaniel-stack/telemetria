package com.daniel.empresas.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.daniel.empresas.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

// Implementa a interface do Spring Security que define como buscar um usuário pelo identificador de login

// O Spring Security chama loadUserByUsername quando precisa autenticar alguém

@Service
@RequiredArgsConstructor

public class UserDetailsServiceImpl implements UserDetailsService {

    // UsuarioRepository é injetado pelo @RequiredArgsConstructor do Lombok
    // final garante que o campo seja injetado pelo construtor — padrão recomendado no Spring
	
    private final UsuarioRepository usuarioRepository;

    @Override
    
    // Se for lançada a exceção, o throws para o método. Como o método retorna um objeto do tipo UserDetails, e um try -
    // catch em caso de exceção, retornaria outra coisa, o código quebraria com um try catch.
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Busca o usuário no banco pelo email
        // orElseThrow lança a exceção se o Optional estiver vazio — ou seja, se o email não existir
        // UsernameNotFoundException é a exceção do Spring Security para esse caso específico
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));
    }
}