package com.grepp.backend5.kafka.config;

import com.grepp.backend5.kafka.dto.OrderEvent;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true")
//@ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConfig {

    @Value("${kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.topic.async-orders:async-orders}")
    private String asyncOrderTopic;

    @Value("${kafka.consumer.group-id:async-sample-group}")
    private String consumerGroupId;

    @Bean
    public ProducerFactory<String, OrderEvent> asyncOrderProducerFactory() {
        // Kafka 프로듀서가 사용할 직렬화 및 연결 정보를 구성한다.
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // 프로듀서가 연결할 브로커 주소
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // 메시지 키를 문자열로 직렬화
//        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // 메시지 값을 JSON으로 직렬화
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class); // 메시지 값을 JSON으로 직렬화
        config.put(JacksonJsonSerializer.ADD_TYPE_INFO_HEADERS, false); // 헤더에 타입 정보를 넣지 않아도 되도록 설정
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, OrderEvent> asyncOrderKafkaTemplate() {
        // ProducerFactory 기반 KafkaTemplate을 생성한다.
        return new KafkaTemplate<>(asyncOrderProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, OrderEvent> asyncOrderConsumerFactory() {
        // Kafka 컨슈머가 사용할 역직렬화 및 그룹 설정을 정의한다.
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // 컨슈머가 붙을 브로커 주소
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId); // 메시지를 함께 소비할 그룹 ID
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // 키를 문자열로 역직렬화
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class); // 값을 JSON으로 역직렬화
        JacksonJsonDeserializer<OrderEvent> deserializer =
                new JacksonJsonDeserializer<>(OrderEvent.class, false);
        deserializer.addTrustedPackages("*");
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> asyncOrderKafkaListenerContainerFactory() {
        // @KafkaListener가 사용할 컨테이너 팩토리를 구성한다.
        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(asyncOrderConsumerFactory()); // 컨슈머 구성 연결
//        factory.setConcurrency(3); // 파티션 병렬 소비 수
        factory.getContainerProperties().setObservationEnabled(true); // Micrometer 관찰 활성화
        return factory;
    }

    @Bean
    public NewTopic asyncOrderTopic() {
        // 개발 환경에서 자동으로 토픽을 생성해 손쉽게 테스트한다.
        return TopicBuilder.name(asyncOrderTopic)
//                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public Clock kafkaSampleClock() {
        // 이벤트 타임스탬프를 UTC로 고정한다.
        return Clock.systemUTC();
    }
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }
}
