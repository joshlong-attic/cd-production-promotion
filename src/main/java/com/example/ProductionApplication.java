package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;


@SpringBootApplication
public class ProductionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductionApplication.class, args);
	}
}

/**
 * you commit a build, it goes through CD pipeline and lands in a distribution repository.
 */
@Configuration
class BuildPromotionConfiguration {

	@Bean
	RestTemplate bintrayRestTemplate(@Value("${bintray.username}") String user,
	                                 @Value("${bintray.password}") String pw) {

		String token = Base64Utils.encodeToString((user + ":" + pw)
				.getBytes(Charset.forName("UTF-8")));
		return authenticatedRestTemplate("Authorization", "Basic " + token);
	}

	private RestTemplate authenticatedRestTemplate(String h, String v) {
		RestTemplate r = new RestTemplate();
		r.getInterceptors().add((request, body, execution) -> {
			request.getHeaders().add(h, v);
			return execution.execute(request, body);
		});
		return r;
	}
}


@RestController
@RequestMapping ("/bintray-wh")
class BintrayWebhookRestController {

	@RequestMapping(method = RequestMethod.POST)
	public void post(RequestEntity<String> requestEntity) {
		System.out.println(requestEntity.getMethod().toString());
		System.out.println(requestEntity.getBody());
		System.out.println(requestEntity.getUrl());
		requestEntity.getHeaders().forEach((name, values) -> values.forEach(System.out::println));
	}
}

@Service
class BuildPromotionService {

	//		http -a joshlong POST https://api.bintray.com/webhooks/swampup-cloud-native-java/maven/demo/ \
//		url=$MY_CUSTOM_BINTRAY_WEBHOOK_WHICH_WILL_BE_A_CF_APP_THAT_DOES_BLUE_GREEN_DEPLOY_OF_APP \
//		method=post

	private final RestTemplate restTemplate;

	/*@PostConstruct
	public void after() throws Exception {
		// swampup-cloud-native-java / maven / cdlive
		String org = "swampup-cloud-native-java ", pkg = "cdlive";
		String url = "https://api.bintray.com/webhooks/" + org ;

		ResponseEntity<String> entity = this.restTemplate.getForEntity(url, String.class);
		System.out.println(
				entity.getBody()
		);
	}
*/
	@Autowired
	public BuildPromotionService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
}


