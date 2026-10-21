package com.example.demo.publishing;

import org.springframework.context.annotation.*;
import java.util.*;

@Configuration
@Profile("!real-publishers")
public class MockPublisherConfiguration {
    @Bean
    List<SocialMediaPublisher> mockPublishers() {
        return Arrays.stream(Platform.values()).<SocialMediaPublisher>map(MockSocialMediaPublisher::new).toList();
    }
}
