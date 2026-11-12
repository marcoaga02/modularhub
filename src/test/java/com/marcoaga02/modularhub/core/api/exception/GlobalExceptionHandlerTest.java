package com.marcoaga02.modularhub.core.api.exception;

import com.marcoaga02.modularhub.core.api.dto.ErrorDTO;
import com.marcoaga02.modularhub.core.api.exception.account.SelfDeletionException;
import com.marcoaga02.modularhub.core.api.exception.identity.IdentityConflictException;
import com.marcoaga02.modularhub.core.api.exception.identity.IdentityProviderUnavailableException;
import com.marcoaga02.modularhub.core.api.exception.identity.IdentityResourceNotFoundException;
import com.marcoaga02.modularhub.core.api.exception.language.LanguageNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleMethodArgumentNotValidException_shouldReturnBadRequestWithDistinctFields_whenFieldsAreInvalid() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "user");
        bindingResult.addError(new FieldError("user", "email", "must be a well-formed email address"));
        bindingResult.addError(new FieldError("user", "email", "must not be blank"));
        bindingResult.addError(new FieldError("user", "username", "must not be blank"));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorDTO> response = handler.handleMethodArgumentNotValidException(exception);

        assertError(response, HttpStatus.BAD_REQUEST, ExceptionCodes.Validation.INVALID_FIELDS,
                Map.of(GlobalExceptionHandler.FIELDS_PARAM, List.of("email", "username")));
    }

    @Test
    void handleInvalidRequestException_shouldReturnBadRequest_whenRequestIsMalformed() {
        ResponseEntity<ErrorDTO> response = handler.handleInvalidRequestException(mock(HttpMessageNotReadableException.class));

        assertError(response, HttpStatus.BAD_REQUEST, ExceptionCodes.Validation.INVALID_REQUEST, Map.of());
    }

    @Test
    void handleNoResourceFoundException_shouldReturnNotFound_whenRouteDoesNotExist() {
        ResponseEntity<ErrorDTO> response = handler.handleNoResourceFoundException(mock(NoResourceFoundException.class));

        assertError(response, HttpStatus.NOT_FOUND, ExceptionCodes.Generic.NOT_FOUND, Map.of());
    }

    @Test
    void handleMethodNotSupportedException_shouldReturnMethodNotAllowedWithAllowHeader_whenMethodIsNotSupported() {
        HttpRequestMethodNotSupportedException exception = mock(HttpRequestMethodNotSupportedException.class);
        when(exception.getSupportedHttpMethods()).thenReturn(Set.of(HttpMethod.GET));

        ResponseEntity<ErrorDTO> response = handler.handleMethodNotSupportedException(exception);

        assertError(response, HttpStatus.METHOD_NOT_ALLOWED, ExceptionCodes.Generic.METHOD_NOT_ALLOWED, Map.of());
        assertThat(response.getHeaders().getAllow()).containsExactly(HttpMethod.GET);
    }

    @Test
    void handleNotFoundException_shouldReturnNotFound_whenResourceIsMissing() {
        ResponseEntity<ErrorDTO> response = handler.handleNotFoundException(
                new IdentityResourceNotFoundException("missing", null));

        assertError(response, HttpStatus.NOT_FOUND, ExceptionCodes.IdentityProvider.NOT_FOUND, Map.of());
    }

    @Test
    void handleBadRequestException_shouldReturnBadRequest_whenDomainRuleIsViolated() {
        ResponseEntity<ErrorDTO> response = handler.handleBadRequestException(new LanguageNotFoundException("lang"));

        assertError(response, HttpStatus.BAD_REQUEST, ExceptionCodes.Model.Language.NOT_FOUND, Map.of());
    }

    @Test
    void handleConflictException_shouldReturnConflictWithParams_whenExceptionHasParams() {
        ConflictException exception = new ConflictException("conflict", "exception.test.conflict", Map.of("value", "x")) {
        };

        ResponseEntity<ErrorDTO> response = handler.handleConflictException(exception);

        assertError(response, HttpStatus.CONFLICT, "exception.test.conflict", Map.of("value", "x"));
    }

    @Test
    void handleConflictException_shouldReturnConflict_whenIdentityProviderReportsConflict() {
        ResponseEntity<ErrorDTO> response = handler.handleConflictException(new IdentityConflictException("conflict", null));

        assertError(response, HttpStatus.CONFLICT, ExceptionCodes.IdentityProvider.CONFLICT, Map.of());
    }

    @Test
    void handleForbiddenException_shouldReturnForbidden_whenOperationIsForbidden() {
        ResponseEntity<ErrorDTO> response = handler.handleForbiddenException(new SelfDeletionException("user-id"));

        assertError(response, HttpStatus.FORBIDDEN, ExceptionCodes.Model.Account.SELF_DELETION, Map.of());
    }

    @Test
    void handleServiceUnavailableException_shouldReturnServiceUnavailable_whenDependencyIsDown() {
        ResponseEntity<ErrorDTO> response = handler.handleServiceUnavailableException(
                new IdentityProviderUnavailableException("down", new RuntimeException()));

        assertError(response, HttpStatus.SERVICE_UNAVAILABLE, ExceptionCodes.IdentityProvider.ERROR, Map.of());
    }

    @Test
    void handleApplicationException_shouldReturnInternalServerError_whenExceptionHasNoCategory() {
        ResponseEntity<ErrorDTO> response = handler.handleApplicationException(new InternalStateException("broken"));

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, ExceptionCodes.Internal.ERROR, Map.of());
    }

    @Test
    void handleAccessDeniedException_shouldReturnForbidden_whenAccessIsDenied() {
        ResponseEntity<ErrorDTO> response = handler.handleAccessDeniedException(new AccessDeniedException("denied"));

        assertError(response, HttpStatus.FORBIDDEN, ExceptionCodes.Access.DENIED, Map.of());
    }

    @Test
    void handleOptimisticLockingFailureException_shouldReturnConflict_whenEntityWasModifiedConcurrently() {
        ResponseEntity<ErrorDTO> response = handler.handleOptimisticLockingFailureException(
                new OptimisticLockingFailureException("stale"));

        assertError(response, HttpStatus.CONFLICT, ExceptionCodes.Generic.CONCURRENT_MODIFICATION, Map.of());
    }

    @Test
    void handleException_shouldReturnInternalServerError_whenExceptionIsUnexpected() {
        ResponseEntity<ErrorDTO> response = handler.handleException(new IllegalStateException("boom"));

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, ExceptionCodes.Internal.ERROR, Map.of());
    }

    private static void assertError(ResponseEntity<ErrorDTO> response, HttpStatus status, String errorCode,
                                    Map<String, Object> params) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(errorCode);
        assertThat(response.getBody().getParams()).isEqualTo(params);
    }
}
