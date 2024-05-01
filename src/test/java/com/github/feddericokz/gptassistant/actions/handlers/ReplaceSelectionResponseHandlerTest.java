package com.github.feddericokz.gptassistant.actions.handlers;

import org.junit.Assert;
import org.junit.Test;

public class ReplaceSelectionResponseHandlerTest {

    private final static String replaceResponseContent = """
                @Test
                void createAndSaveGrowCycleTest() throws Exception {
                    // 1. Create a list of grow cycles
                    List&lt;GrowCycle&gt; growCycles = new ArrayList&lt;&gt;();
                    growCycles.add(new GrowCycle(1L, Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().plusDays(10)), "Cycle 1"));
                    growCycles.add(new GrowCycle(2L, Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().plusDays(15)), "Cycle 2"));
                   \s
                    // 2. Prepare the HTTP request with the grow cycles list as payload
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Content-Type", "application/json");
                    HttpEntity&lt;List&lt;GrowCycle&gt;&gt; requestEntity = new HttpEntity&lt;&gt;(growCycles, headers);
                   \s
                    // 3. Make the HTTP request to save the grow cycles
                    RestTemplate restTemplate = new RestTemplate();
                    String serverUrl = "http://localhost:8080/api/growcycles/saveAll"; // Assuming a provided endpoint URL for saving the grow cycles
                    ResponseEntity&lt;String&gt; response = restTemplate.exchange(serverUrl, HttpMethod.POST, requestEntity, String.class);
                   \s
                    // 4. Serialize the response to JSON and print
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonResponse = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response.getBody());
                    System.out.println("Response from Server: " + jsonResponse);
                   \s
                    // 5. Assert the response status code is OK (200)
                    Assertions.assertEquals(200, response.getStatusCodeValue());
                }
            """;

    @Test
    public void testSanitizeSelection() {
        String sanitizedString = ReplaceSelectionResponseHandler.sanitizeUpdateSelection(replaceResponseContent);
        Assert.assertFalse(sanitizedString.contains("&lt;"));
        Assert.assertFalse(sanitizedString.contains("&gt;"));
    }

}
