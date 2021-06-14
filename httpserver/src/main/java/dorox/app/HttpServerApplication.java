package dorox.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = { "org.n3r.idworker","dorox.app"})
public class HttpServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(HttpServerApplication.class, args);
	}
	
	@Bean
	RedisTemplate<String, Object> redisTemplate( RedisConnectionFactory connectionFactory) {
		 RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
	        redisTemplate.setConnectionFactory(connectionFactory);
	        redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
	        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
	        redisTemplate.setKeySerializer(stringRedisSerializer);
	        redisTemplate.setHashKeySerializer(stringRedisSerializer);
	        return redisTemplate;
	}

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
