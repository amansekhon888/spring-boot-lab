package com.example.demo.publishing;

/** A tiny interface so the consumer can publish without knowing a real platform API. */
public interface SocialMediaPublisher {
    String publish(Platform platform, String content);
}
