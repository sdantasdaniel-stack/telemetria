package com.daniel.empresas.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;


// Ao usar @ControllerAdvice, você comunica ao framework: Centralização de Erros: "Se qualquer 
//controller lançar uma exceção X, não deixe ele lidar com isso sozinho; use o método que eu defini aqui com @ExceptionHandler"."
//Isso evita códigos duplicados de try-catch em cada classe.

@ControllerAdvice
public class GlobalExceptionHandler {
	
	// Com essa anotação, Quando essa exceção for lançada em qualquer lugar do sistema, o Spring chama esse método.
	// O .class é a forma do Java de referenciar a classe em si, não uma instância dela
	@ExceptionHandler(CnpjJaCadastradoException.class)

	// ResponseEntity é uma classe do Spring que representa a resposta HTTP completa — não só o corpo, mas também o status code e os cabeçalhos.
	// <ErroResponse> diz que o corpo do ResponseEntity vai ser um objeto ErroResponse
	// ex é o objeto da exceção que foi lançada — o Spring injeta ele aqui automaticamente
	public ResponseEntity<ErroResponse> handleCnpjJaCadastrado(CnpjJaCadastradoException ex) {

	    // new ResponseEntity<>() cria o envelope da resposta
	    // o <> vazio é o diamond operator — o Java já sabe o tipo pelo retorno declarado acima, não precisa repetir
	    return new ResponseEntity<>(

	        // new ErroResponse() cria o corpo da resposta com três campos:
	        // HttpStatus.CONFLICT.value() — converte a constante CONFLICT para o número inteiro 409
	        // ex.getMessage() — recupera a mensagem que foi escrita no Service quando a exceção foi lançada
	        // LocalDateTime.now() — registra o momento exato em que o erro ocorreu
	        new ErroResponse(HttpStatus.CONFLICT.value(), ex.getMessage(), LocalDateTime.now()),

	        // segundo argumento do ResponseEntity — define o status HTTP real do cabeçalho da resposta
	        // é diferente do status dentro do ErroResponse: esse aqui é o que o navegador e o cliente HTTP leem
	        HttpStatus.CONFLICT
	    );
	}
	
	
	@ExceptionHandler(EmpresaNaoEncontradaException.class)
	public ResponseEntity<ErroResponse> handleEmpresaNaoEncontradaException (EmpresaNaoEncontradaException ex) {
		
		return new ResponseEntity<>(
				
				new ErroResponse(
						
						HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now()),
						HttpStatus.NOT_FOUND
						
						);
	}
	
	@ExceptionHandler(DeviceNaoEncontradoException.class)
	public ResponseEntity<ErroResponse> handleDeviceNaoEncontradoException (DeviceNaoEncontradoException ex) {
		
		return new ResponseEntity<>(
				
				new ErroResponse(
						
						HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now()),
						HttpStatus.NOT_FOUND
						
						);
	}
	
	@ExceptionHandler(UsuarioNaoEncontradoException.class)
	public ResponseEntity<ErroResponse> handleUsuarioNaoEncontradoException (UsuarioNaoEncontradoException ex) {
		
		return new ResponseEntity<>(
				
				new ErroResponse(
						
						HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now()),
						HttpStatus.NOT_FOUND
						
						);
	}
	
	@ExceptionHandler(EmailJaCadastradoException.class)
	public ResponseEntity<ErroResponse> handleEmailJaCadastradoException (EmailJaCadastradoException ex) {
		
		return new ResponseEntity<>(
				
				new ErroResponse(
						
						HttpStatus.CONFLICT.value(), ex.getMessage(), LocalDateTime.now()),
						HttpStatus.CONFLICT
						
						);
	}
	
	@ExceptionHandler(IdentificadorJaCadastradoException.class)
	public ResponseEntity<ErroResponse> handleIdentificadorJaCadastradoException (IdentificadorJaCadastradoException ex) {
		
		return new ResponseEntity<>(
				
				new ErroResponse(
						
						HttpStatus.CONFLICT.value(), ex.getMessage(), LocalDateTime.now()),
						HttpStatus.CONFLICT
						
						);
	}

	// Serve para qualquer erro de digitação nos campos RoleEnum e StatusEnum.
	// Diferente dos outros erros citados aqui que ocorrem no service, o JSON chegou com um valor que o Jackson não consegue converter para
	//RoleEnum ou StatusEnum O erro acontece antes do Controller, na deserialização. O Service nunca é chamado.
	// Jackson é a bib. que transforma objetos Java em JSON (serialização) e JSON em objetos Java (desserialização) com rapidez e flexibilidade.
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErroResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
	    return new ResponseEntity<>(
	        new ErroResponse(HttpStatus.BAD_REQUEST.value(), "Valor inválido enviado no corpo da requisição", LocalDateTime.now()),
	        HttpStatus.BAD_REQUEST
	    );
	}
	
	// adicionei para corrigir o erro que aparece quando há problema de autenticação
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErroResponse> handleBadCredentials(BadCredentialsException ex) {
	    return new ResponseEntity<>(
	        new ErroResponse(HttpStatus.UNAUTHORIZED.value(), "Email ou senha inválidos", LocalDateTime.now()),
	        HttpStatus.UNAUTHORIZED
	    );
	}
	
	// Captura erros quando o cliente usa um método HTTP que o endpoint não suporta (ex: PATCH, DELETE, PUT em um endpoint que só aceita GET).
	
	// HttpRequestMethodNotSupportedException já vem pronta no Spring — não precisa criar manualmente.
	
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErroResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
	    
	    String mensagem;
	    
	    // verifica se o método não suportado foi PATCH
	    // se sim, indica que a API não suporta atualização parcial
	    // O ex.getMethod() retorna o método que foi tentado — "PATCH", "DELETE", "PUT" etc. Assim a mensagem específica só aparece quando o 
	    //cliente tentou usar PATCH, e qualquer outro método errado recebe a mensagem genérica.
	    if ("PATCH".equals(ex.getMethod())) {
	        mensagem = "Atualização parcial não é suportada. Para atualizar uma entidade você precisa digitar todos os campos no corpo da requisição e usar o método PUT.";
	    } else {
	        mensagem = "Método HTTP não suportado para este endpoint. Verifique a documentação da API.";
	    }
	    
	    return new ResponseEntity<>(
	        new ErroResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), mensagem, LocalDateTime.now()),
	        HttpStatus.METHOD_NOT_ALLOWED
	    );
	}
	
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErroResponse> handleNotFound(NoHandlerFoundException ex) {
	    return ResponseEntity.status(404)
	            .body(new ErroResponse(404, "Endpoint não encontrado", LocalDateTime.now()));
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroResponse> handleValidacao(MethodArgumentNotValidException ex) {
	    String mensagem = ex.getBindingResult().getFieldErrors().stream()
	        .map(f -> f.getField() + ": " + f.getDefaultMessage())
	        .collect(Collectors.joining(", "));
	    return new ResponseEntity<>(
	        new ErroResponse(HttpStatus.BAD_REQUEST.value(), mensagem, LocalDateTime.now()),
	        HttpStatus.BAD_REQUEST
	    );
	}
	
	// para operações com id válido mas que nao foram realizadas (ex.: tentar desativar um device que já estava desativado)
	@ExceptionHandler(EstadoInvalidoException.class)
	public ResponseEntity<ErroResponse> handleEstadoInvalido(EstadoInvalidoException ex) {
	    return new ResponseEntity<>(
	        new ErroResponse(HttpStatus.CONFLICT.value(), ex.getMessage(), LocalDateTime.now()),
	        HttpStatus.CONFLICT
	    );
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErroResponse> handleAccessDenied(AccessDeniedException ex) {
	    return new ResponseEntity<>(
	        new ErroResponse(HttpStatus.FORBIDDEN.value(), "Acesso negado: sua role não permite realizar esta ação", LocalDateTime.now()),
	        HttpStatus.FORBIDDEN
	    );
	}

	
	
	// captura qualquer exceção não prevista pelos handlers específicos acima
	// sempre deve ser o último método da classe para não interceptar exceções que têm handler próprio
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroResponse> handleException(Exception ex) {
	    return new ResponseEntity<>(
	        new ErroResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
	            "Erro interno no servidor", LocalDateTime.now()),
	        HttpStatus.INTERNAL_SERVER_ERROR
	    );
	}
	
	
				
				
				
		
		
	}


