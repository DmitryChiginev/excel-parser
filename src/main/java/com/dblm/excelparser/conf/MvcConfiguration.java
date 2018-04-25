package com.dblm.excelparser.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.nio.charset.Charset;

/**
 * Конфигурация веб-приложения.
 */
@Configuration
@EnableWebMvc
@ComponentScan("com.dblm.excelparser.")
@PropertySource("classpath:application.properties")
public class MvcConfiguration extends WebMvcConfigurerAdapter {

    @Value("${app.startup.port}")
    private Integer port;

    /**
     * Статические ресурсы.
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
        registry.addResourceHandler("/WEB-INF/**").addResourceLocations("/WEB-INF/**");
        registry.addResourceHandler("/frameworks/**").addResourceLocations("/frameworks/");
    }

    /**
     * Вьюхи.
     *
     * @return
     */
    @Bean
    public ViewResolver configureViewResolver() {
        InternalResourceViewResolver viewResolve = new InternalResourceViewResolver();
        viewResolve.setPrefix("/WEB-INF/views/");
        viewResolve.setSuffix(".jsp");
        viewResolve.setViewClass(JstlView.class);

        return viewResolve;
    }

    /**
     * Настраиваем контейнер сервлетов.
     *
     * @return
     */
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.setPort(port);
        factory.setUriEncoding(Charset.forName("UTF-8"));
        return factory;
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer){
        configurer.enable();
    }
}
