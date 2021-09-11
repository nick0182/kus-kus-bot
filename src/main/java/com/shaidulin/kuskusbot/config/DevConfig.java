package com.shaidulin.kuskusbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
@Profile("dev")
public class DevConfig extends BaseConfig {

    @Bean
    LettuceConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory lettuceConnectionFactory =
                new LettuceConnectionFactory(new RedisStandaloneConfiguration(redisHost));
        lettuceConnectionFactory.setConvertPipelineAndTxResults(false);
        return lettuceConnectionFactory;
    }
}
