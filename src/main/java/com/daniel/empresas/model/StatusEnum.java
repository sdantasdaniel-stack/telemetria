package com.daniel.empresas.model;

//Enum é usado aqui porque os valores possíveis são fixos e conhecidos em tempo de compilação:
//um usuário só pode ser ADMIN ou USER, e um device só pode ser ONLINE ou OFFLINE.
//Diferente de uma classe ou record, o enum impede que qualquer outro valor seja atribuído,
//garantindo segurança em tempo de compilação. Uma classe ou record permitiria criar
//instâncias com valores arbitrários, o que não faz sentido para esses casos.

public enum StatusEnum {
	ONLINE,
	OFFLINE

}
