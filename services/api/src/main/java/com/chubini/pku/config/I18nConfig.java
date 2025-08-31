package com.chubini.pku.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Internationalization configuration for the PKU Diet App Supports English (en), Georgian (ka), and
 * Russian (ru) locales
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

  /** Configure message source for localized messages */
  @Bean
  public ReloadableResourceBundleMessageSource messageSource() {
    ReloadableResourceBundleMessageSource messageSource =
        new ReloadableResourceBundleMessageSource();

    // Set the base name of the resource bundle
    messageSource.setBasename("classpath:messages");

    // Set the default encoding
    messageSource.setDefaultEncoding("UTF-8");

    // Set cache duration (in seconds) for messages
    messageSource.setCacheSeconds(3600); // 1 hour

    // Set whether to use the message code as default message if not found
    messageSource.setUseCodeAsDefaultMessage(true);

    // Set fallback to system locale if not found
    messageSource.setFallbackToSystemLocale(true);

    return messageSource;
  }

  /** Configure locale resolver to use Accept-Language header */
  @Bean
  public AcceptHeaderLocaleResolver localeResolver() {
    AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();

    // Set default locale to English
    localeResolver.setDefaultLocale(Locale.ENGLISH);

    // Set supported locales
    localeResolver.setSupportedLocales(
        java.util.Arrays.asList(
            Locale.ENGLISH,
            new Locale("ka"), // Georgian
            new Locale("ru") // Russian
            ));

    return localeResolver;
  }
}
