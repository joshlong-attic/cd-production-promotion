package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BintrayWebhookRestController {

	@RequestMapping(method = RequestMethod.POST, value = "/bintray-webhook")
	public void bintrayCallback(RequestEntity<String> body)
			throws Exception {

		System.out.println("the body is " + body);
//		http -a joshlong POST https://api.bintray.com/webhooks/swampup-cloud-native-java/maven/demo/ \
//		url=$MY_CUSTOM_BINTRAY_WEBHOOK_WHICH_WILL_BE_A_CF_APP_THAT_DOES_BLUE_GREEN_DEPLOY_OF_APP \
//		method=post


	}


	private final BintrayService bintray;

	@Autowired
	public BintrayWebhookRestController(BintrayService service) {
		this.bintray = service;
	}
}
