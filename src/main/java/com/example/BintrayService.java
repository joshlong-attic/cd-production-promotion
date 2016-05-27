package com.example;

import com.example.bintray.Bintray;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BintrayService implements InitializingBean {

	private final RestTemplate restTemplate ;

	@Autowired
	public BintrayService( @Bintray RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// lets register the webhook if possible

	}
}
