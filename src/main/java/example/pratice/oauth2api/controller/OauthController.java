package example.pratice.oauth2api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OauthController {

	@GetMapping("/")
	public String getOauthLoginPage(){
		return "/index";
	}
}
