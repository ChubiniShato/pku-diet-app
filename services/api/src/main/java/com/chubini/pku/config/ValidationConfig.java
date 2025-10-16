package com.chubini.pku.config;

import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

@Configuration
public class ValidationConfig {

  @Bean
  public LocalValidatorFactoryBean validator() {
    return new LocalValidatorFactoryBean();
  }

  @Bean
  public MethodValidationPostProcessor methodValidationPostProcessor() {
    MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
    processor.setValidator(validator());
    return processor;
  }

  /** Utility method to validate objects programmatically */
  public static <T> void validateObject(T object, Validator validator) {
    Set<ConstraintViolation<T>> violations = validator.validate(object);
    if (!violations.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (ConstraintViolation<T> violation : violations) {
        sb.append(violation.getPropertyPath())
            .append(": ")
            .append(violation.getMessage())
            .append("; ");
      }
      throw new ConstraintViolationException("Validation failed: " + sb.toString(), violations);
    }
  }
}
