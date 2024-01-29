package org.tasker.updates.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.server.ResponseStatusException;
import org.tasker.updates.service.ValidationService;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {
    private final Validator validator;

    @Override
    public void validate(Object target, String objectName) {
        Errors errors = new BeanPropertyBindingResult(target, objectName);
        validator.validate(target, errors);
        if (errors.hasErrors()) {
            FieldError fieldError = errors.getFieldError();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldError == null ? "Invalid request" : fieldError.getDefaultMessage());
        }
    }
}
