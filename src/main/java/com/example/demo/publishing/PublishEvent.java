package com.example.demo.publishing;

/** The first Kafka lesson sends only an id; the database remains the source of post content. */
public record PublishEvent(Long publicationId) { }
