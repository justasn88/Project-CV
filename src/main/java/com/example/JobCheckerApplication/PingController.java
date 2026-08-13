package com.example.JobCheckerApplication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
// comment
@RestController
public class PingController {

	@GetMapping("/ping")
	public String ping() {
		return "Serveris veikia!";
	}
}
