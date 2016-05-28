package com.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


@SpringBootApplication
public class ProductionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductionApplication.class, args);
	}

	@Bean
	RestTemplate bintrayRestTemplate(@Value("${bintray.username}") String user,
	                                 @Value("${bintray.password}") String pw) {

		String token = Base64Utils.encodeToString((user + ":" + pw)
				.getBytes(Charset.forName("UTF-8")));

		return authenticatedRestTemplate("Authorization", "Basic " + token);

	}


	@Bean
	CommandLineRunner commandLineRunner(RestTemplate restTemplate) {
		return args -> {

			Function<String, String> urlBuilder = uri -> "https://api.bintray.com" + uri;

			String subject = "swampup-cloud-native-java";
			String repo = "maven";
			String pkg = "cdlive";

			Function<Void, List<Map<String, Object>>> readerWebhooks = v -> {

				String apiRoot = urlBuilder.apply("/webhooks/{subject}/{repo}/{package}");
				ParameterizedTypeReference<List<Map<String, Object>>> ptr =
						new ParameterizedTypeReference<List<Map<String, Object>>>() {
						};
				ResponseEntity<List<Map<String, Object>>> entity = restTemplate.exchange(
						apiRoot,
						HttpMethod.GET,
						null,
						ptr,
						subject,
						repo,
						pkg
				);
				isTrue(entity.getStatusCode().is2xxSuccessful());
				return entity.getBody();
			};


			// http -a joshlong POST https://api.bintray.com/webhooks/swampup-cloud-native-java/maven/demo/ url=$WH method=post
			List<Map<String, Object>> list = readerWebhooks.apply(null);

			if (list.size() == 0) {

				// POST /webhooks/:subject/:repo/:package

				String post = urlBuilder.apply("/webhooks/{subject}/{repo}/{package}");

				Map<String, String> body = new HashMap<>();
				body.put("url", "http://cnj-cd-production-promotion.cfapps.io/bintray-webhook");
				body.put("method", "POST");

				ResponseEntity<String> postForEntity = restTemplate.postForEntity(post,
						body, String.class, subject, repo , pkg );
				log.info("response from POST: " + postForEntity.getBody());

				readerWebhooks.apply(null).forEach(m -> m.forEach((k, v) -> log.info(k + '=' + v)));
			}


		};

	}

	private Log log = LogFactory.getLog(getClass());

	private RestTemplate authenticatedRestTemplate(String h, String v) {
		RestTemplate r = new RestTemplate();
		r.getInterceptors().add((request, body, execution) -> {
			request.getHeaders().add(h, v);
			return execution.execute(request, body);
		});
		return r;
	}
}

/**
 * you commit a build, it goes through CD pipeline and lands in a distribution repository.
 */

@RestController
class BintrayWebhookRestController {

	private Log log = LogFactory.getLog(getClass());

	// this is registered at
	// https://cnj-cd-production-promotion.cfapps.io/bintray-webhook

	@RequestMapping(method = POST, value = "/bintray-webhook")
	public void post(RequestEntity<Map<String, String>> re) {
		log.info("=========================");
		log.info("Start");
		log.info("=========================");
		re.getBody().forEach((name, v) -> log.info(name + '=' + v));
		log.info("------------------------");
		log.info(re.getMethod().toString());
		log.info(re.getUrl());
		re.getHeaders().forEach((name, values) -> values.forEach(log::info));
		log.info("=========================");
		log.info("End");
		log.info("=========================");
	}
}

@Service
class BuildPromotionService {

	// http -a joshlong POST https://api.bintray.com/webhooks/swampup-cloud-native-java/maven/demo/ url=$WH method=post

	private final RestTemplate restTemplate;

	@Autowired
	public BuildPromotionService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
}


