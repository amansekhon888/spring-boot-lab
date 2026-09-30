package com.example.demo.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/** Turns on Spring's cache annotations; Ehcache supplies the bounded store configured in ehcache.xml. */
@Configuration
@EnableCaching
public class CacheConfiguration {
}