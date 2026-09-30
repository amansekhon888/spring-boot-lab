package com.example.demo.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

// This filter generates a unique correlation ID for each incoming HTTP request and adds it to the MDC (Mapped Diagnostic Context) for logging purposes. If the request already contains a correlation ID in the "X-Correlation-ID" header, it uses that value instead of generating a new one. The correlation ID is removed from the MDC after the request is processed to avoid memory leaks.

// MDC - Mapped Diagnostic Context is a feature provided by logging frameworks like SLF4J and Logback that allows you to store contextual information (like a correlation ID) for the duration of a request or thread. This information can then be included in log messages, making it easier to trace and debug issues in distributed systems.
@Component
public class CorrelationIdFilter implements Filter {

    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CORRELATION_ID_KEY, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
}