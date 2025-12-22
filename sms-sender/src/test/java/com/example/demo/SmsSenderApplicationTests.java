package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SmsSenderApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void testValidPhoneNumber() throws Exception {
		String validRequest = """
					{
						"phoneNumber": "+1234567890",
						"message": "Hello World"
					}
				""";

		mockMvc.perform(post("/v1/sms/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content(validRequest))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("success"));
	}

	@Test
	void testInvalidPhoneNumber_StartsWithZero() throws Exception {
		String invalidRequest = """
					{
						"phoneNumber": "0123456789",
						"message": "Hello World"
					}
				""";

		mockMvc.perform(post("/v1/sms/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
				.andExpect(status().isBadRequest());
	}

	@Test
	void testInvalidPhoneNumber_TooShort() throws Exception {
		String invalidRequest = """
					{
						"phoneNumber": "+12",
						"message": "Hello World"
					}
				""";

		mockMvc.perform(post("/v1/sms/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
				.andExpect(status().isBadRequest());
	}

	@Test
	void testInvalidPhoneNumber_TooLong() throws Exception {
		String invalidRequest = """
					{
						"phoneNumber": "+12345678901234567890",
						"message": "Hello World"
					}
				""";

		mockMvc.perform(post("/v1/sms/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
				.andExpect(status().isBadRequest());
	}

	@Test
	void testInvalidPhoneNumber_ContainsLetters() throws Exception {
		String invalidRequest = """
					{
						"phoneNumber": "+123abc7890",
						"message": "Hello World"
					}
				""";

		mockMvc.perform(post("/v1/sms/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
				.andExpect(status().isBadRequest());
	}

	@Test
	void testInvalidPhoneNumber_ContainsSpecialChars() throws Exception {
		String invalidRequest = """
					{
						"phoneNumber": "+123-456-7890",
						"message": "Hello World"
					}
				""";

		mockMvc.perform(post("/v1/sms/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
				.andExpect(status().isBadRequest());
	}

	@Test
	void testMissingPhoneNumber() throws Exception {
		String invalidRequest = """
					{
						"message": "Hello World"
					}
				""";

		mockMvc.perform(post("/v1/sms/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
				.andExpect(status().isBadRequest());
	}

	@Test
	void testEmptyPhoneNumber() throws Exception {
		String invalidRequest = """
					{
						"phoneNumber": "",
						"message": "Hello World"
					}
				""";

		mockMvc.perform(post("/v1/sms/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
				.andExpect(status().isBadRequest());
	}

	@Test
	void testMissingMessage() throws Exception {
		String invalidRequest = """
					{
						"phoneNumber": "+1234567890"
					}
				""";

		mockMvc.perform(post("/v1/sms/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidRequest))
				.andExpect(status().isBadRequest());
	}
}
