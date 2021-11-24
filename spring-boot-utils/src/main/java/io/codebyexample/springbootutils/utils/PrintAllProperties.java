package io.codebyexample.springbootutils.utils;

import ch.qos.logback.classic.Logger;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class PrintAllProperties {

  public static final Pattern SENSITIVE_WORD_PATTERN = Pattern
      .compile("hashKey|token|password|username|clientKeys", Pattern.CASE_INSENSITIVE);

  private ConfigurableEnvironment configurableEnvironment;

  @EventListener
  public void printAllProperties(ContextRefreshedEvent event) {
    final Environment env = event.getApplicationContext().getEnvironment();
    log.info("====== Environment and configuration ======");
    log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
    final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();
    StreamSupport.stream(sources.spliterator(), false)
        .filter(ps -> ps instanceof EnumerablePropertySource)
        .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
        .flatMap(Arrays::stream)
        .distinct()
        .filter(prop -> !(prop.contains("credentials") || prop.contains("password")))
        .forEach(prop -> log.info(configFormat(prop, env.getProperty(prop))));
    log.info("===========================================");
  }

  public static String configFormat(String key, String val) {
    if (SENSITIVE_WORD_PATTERN.matcher(key).find()) {
      return key + "=***";
    }

    return key + "=" + val;
  }
}
