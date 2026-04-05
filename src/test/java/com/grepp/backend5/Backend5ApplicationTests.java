package com.grepp.backend5;

import com.grepp.backend5.search.infrastructure.ProductSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"eureka.client.enabled=false",
		"kafka.enabled=false",
		"spring.sql.init.schema-locations=classpath:org/springframework/batch/core/schema-postgresql.sql"
})
class Backend5ApplicationTests {

	@MockitoBean
	private ElasticsearchOperations elasticsearchOperations;

	@MockitoBean
	private ProductSearchRepository productSearchRepository;

	@Test
	void contextLoads() {
	}

}
