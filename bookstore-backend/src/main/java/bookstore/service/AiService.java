package bookstore.service;

import bookstore.dto.ChatRequest.BookDtoSimple;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;

@Service
public class AiService {

    @Value("${google.ai.api-key}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String callGemini(String userMessage, List<BookDtoSimple> books) {
        try {
            // ... (Context building code remains same, omitted for brevity if using
            // replace, but I need to be careful with replace)
            // Ideally I should precise target, but let's rewrite the method or just the URL
            // and the catch block.
            // Let's rewrite the URL line and the response handling block.

            // 1. Construct System Prompt (Context)
            StringBuilder context = new StringBuilder();
            context.append("你是『BookStore』的專業購書顧問。請根據以下庫存書籍清單回答使用者的問題：\n");

            if (books != null) {
                for (BookDtoSimple b : books) {
                    context.append(String.format("- 書名:%s, 作者:%s, 價格:%d, 簡介:%s\n",
                            b.getBookName(), b.getAuthor(), b.getPrice(), b.getShortDesc()));
                }
            }
            context.append("\n規則：\n1. 若使用者詢問的問題超出清單範圍，請禮貌告知並推薦清單內最接近的書。\n");
            context.append("2. 請用繁體中文回答。\n");
            context.append("3. 請用 Markdown 格式美化排版 (如粗體書名)。\n");
            context.append("4. 若沒有相關書籍，請明確說「目前沒有庫存」。\n");

            ObjectNode rootNode = objectMapper.createObjectNode();
            ArrayNode contentsNode = rootNode.putArray("contents");
            ObjectNode contentNode = contentsNode.addObject();
            ArrayNode partsNode = contentNode.putArray("parts");
            ObjectNode partNode = partsNode.addObject();

            String fullPrompt = context.toString() + "\n使用者問題：" + userMessage;
            partNode.put("text", fullPrompt);

            String jsonBody = objectMapper.writeValueAsString(rootNode);

            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(GEMINI_API_URL + "?key=" + apiKey)
                    .post(body)
                    .build();

            // DEBUG LOGGING
            System.out.println("AI Request URL: " + GEMINI_API_URL);
            System.out.println("AI API Key Length: " + (apiKey != null ? apiKey.length() : "null"));

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "null";
                    System.err.println("AI Error Code: " + response.code());
                    System.err.println("AI Error Body: " + errorBody);
                    return "AI 服務暫時無法使用 (Code: " + response.code() + ", Message: " + errorBody + ")";
                }

                String responseBody = response.body().string();
                JsonNode resNode = objectMapper.readTree(responseBody);
                JsonNode candidates = resNode.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode firstCandidate = candidates.get(0);
                    JsonNode parts = firstCandidate.path("content").path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        return parts.get(0).path("text").asText();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "AI 連線發生錯誤：" + e.getMessage();
        }
        return "無法產生回應 (No Candidates)。";
    }
}
