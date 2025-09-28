package com.chubini.pku.security;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/** Rate limiting filter. This class is referenced by other components but was missing. */
@Component
public class RateLimitingFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    // Rate limiting logic will be implemented in future PRs
    chain.doFilter(request, response);
  }
}
