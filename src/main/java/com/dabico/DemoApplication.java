package com.dabico;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@SpringBootApplication
@RestController
public class DemoApplication {

	OkHttpClient client = new OkHttpClient();
	String clientSecret = "56583668e32b73702785a85900975d1ceccf15d5";

	@GetMapping("/")
	void init(){
	}

	@GetMapping("/home")
	String home() throws IOException {
		Request request = new Request.Builder()
				.url("https://api.github.com/search/repositories?q=language:Java&sort=stars&order=desc&per_page=100")
				.addHeader("Authorization",clientSecret)
				.addHeader("Accept","application/vnd.github.v3+json")
				.build();

		Call call = client.newCall(request);
		Response response = call.execute();

		String responseString = response.body() != null ? response.body().string() : "{}";
		if (response.body() != null){
			JsonObject bodyJson = JsonParser.parseString(responseString).getAsJsonObject();
			return bodyJson.get("items").toString();
		} else {
			return "No Results";
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}