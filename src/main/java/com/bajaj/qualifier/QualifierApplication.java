package com.bajaj.qualifier;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class QualifierApplication {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String INIT_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    public static void main(String[] args) {
        SpringApplication.run(QualifierApplication.class, args);
    }

    @PostConstruct
    public void init() {
        generateAndSubmit();
    }

    private void generateAndSubmit() {
        // Step 1: Prepare request to generate webhook
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "Somya Kedia");
        requestBody.put("regNo", "22ucc126");
        requestBody.put("email", "22ucc126@lnmiit.ac.in");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(INIT_URL, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String webhookUrl = (String) response.getBody().get("webhook");
            String accessToken = (String) response.getBody().get("accessToken");

            // Step 2: Final SQL query for your regNo (even)
            String finalQuery = "SELECT e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, d.DEPARTMENT_NAME, " +
                    "COUNT(CASE WHEN e2.DOB > e.DOB THEN 1 END) AS YOUNGER_EMPLOYEES_COUNT " +
                    "FROM EMPLOYEE e " +
                    "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                    "LEFT JOIN EMPLOYEE e2 ON e.DEPARTMENT = e2.DEPARTMENT " +
                    "GROUP BY e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, d.DEPARTMENT_NAME " +
                    "ORDER BY e.EMP_ID DESC;";

            // Step 3: Submit your final SQL query to webhook URL with JWT token
            Map<String, String> answerBody = new HashMap<>();
            answerBody.put("finalQuery", finalQuery);

            HttpHeaders answerHeaders = new HttpHeaders();
            answerHeaders.setContentType(MediaType.APPLICATION_JSON);
            answerHeaders.setBearerAuth(accessToken);

            HttpEntity<Map<String, String>> answerEntity = new HttpEntity<>(answerBody, answerHeaders);
            ResponseEntity<String> submitResponse = restTemplate.exchange(webhookUrl, HttpMethod.POST, answerEntity, String.class);

            System.out.println("Submission status: " + submitResponse.getStatusCode());
            System.out.println("Response body: " + submitResponse.getBody());
        } else {
            System.out.println("Error during webhook generation: " + response.getStatusCode());
        }
    }
}
