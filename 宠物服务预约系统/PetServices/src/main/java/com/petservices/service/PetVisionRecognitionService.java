package com.petservices.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petservices.dto.PetVisionRecognitionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PetVisionRecognitionService {

    private static final Logger log = LoggerFactory.getLogger(PetVisionRecognitionService.class);

    private static final String QWEN_API_KEY = "sk-43593b9128a34c92a9ef9607e32a32d4";
    private static final String QWEN_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String MODEL = "qwen-vl-plus";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PetVisionRecognitionDto recognize(MultipartFile file) {
        try {
            PetVisionRecognitionDto qwenResult = recognizeByQwen(file);
            if (qwenResult != null) {
                return qwenResult;
            }
        } catch (Exception e) {
            log.warn("Qwen VL pet vision recognition failed, use fallback result", e);
        }
        return fallback(file == null ? "" : file.getOriginalFilename());
    }

    private PetVisionRecognitionDto recognizeByQwen(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.trim().isEmpty()) {
            contentType = "image/jpeg";
        }
        String imageBase64 = Base64.getEncoder().encodeToString(file.getBytes());
        String imageUrl = "data:" + contentType + ";base64," + imageBase64;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(QWEN_API_KEY);

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("temperature", 0.2);
        body.put("messages", buildMessages(imageUrl));

        String response = restTemplate.postForObject(QWEN_URL, new HttpEntity<>(body, headers), String.class);
        JsonNode root = objectMapper.readTree(response);
        String content = root.path("choices").path(0).path("message").path("content").asText();
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        JsonNode json = objectMapper.readTree(extractJson(content));
        return buildDtoFromQwenJson(json);
    }

    private List<Map<String, Object>> buildMessages(String imageUrl) {
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");

        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("type", "image_url");
        Map<String, String> imageUrlMap = new HashMap<>();
        imageUrlMap.put("url", imageUrl);
        imagePart.put("image_url", imageUrlMap);
        content.add(imagePart);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("type", "text");
        textPart.put("text", "请分析这张宠物图片，识别宠物物种、品种、毛色、预估年龄阶段、体型，并基于品种大数据生成AI性格侧写。请只返回JSON，不要Markdown，不要解释。JSON字段必须包含：species(猫/狗/其他), breed, color, size, ageEstimate(幼年/成年/老年/未知), age(数字，无法判断填3), traits(3个性格关键词数组), personalitySummary, confidence(0到1), rawName。");
        content.add(textPart);

        userMessage.put("content", content);
        messages.add(userMessage);
        return messages;
    }

    private String extractJson(String content) {
        String text = content.trim();
        if (text.startsWith("```")) {
            int first = text.indexOf('{');
            int last = text.lastIndexOf('}');
            if (first >= 0 && last > first) {
                return text.substring(first, last + 1);
            }
        }
        int first = text.indexOf('{');
        int last = text.lastIndexOf('}');
        if (first >= 0 && last > first) {
            return text.substring(first, last + 1);
        }
        return text;
    }

    private PetVisionRecognitionDto buildDtoFromQwenJson(JsonNode json) {
        PetVisionRecognitionDto dto = new PetVisionRecognitionDto();
        dto.setSpecies(json.path("species").asText("其他"));
        dto.setBreed(json.path("breed").asText("未知"));
        dto.setColor(json.path("color").asText("需人工确认"));
        dto.setSize(json.path("size").asText("需人工确认"));
        dto.setAgeEstimate(json.path("ageEstimate").asText("成年"));
        dto.setAge(json.path("age").asInt(resolveAge(dto.getAgeEstimate())));
        dto.setPersonalitySummary(json.path("personalitySummary").asText(buildPersonality(dto.getBreed(), dto.getSpecies(), null)));
        dto.setConfidence(json.path("confidence").asDouble(0.8D));
        dto.setSource("通义千问视觉模型 qwen-vl-plus");
        dto.setRawName(json.path("rawName").asText(dto.getBreed()));

        List<String> traits = new ArrayList<>();
        JsonNode traitNode = json.path("traits");
        if (traitNode.isArray()) {
            for (JsonNode item : traitNode) {
                traits.add(item.asText());
            }
        }
        if (traits.isEmpty()) {
            traits = resolveTraits(dto.getBreed(), dto.getSpecies());
        }
        dto.setTraits(traits);
        return dto;
    }

    private int resolveAge(String ageEstimate) {
        if ("幼年".equals(ageEstimate)) return 1;
        if ("老年".equals(ageEstimate)) return 8;
        return 3;
    }

    private List<String> resolveTraits(String name, String species) {
        String n = name == null ? "" : name;
        if (n.contains("金毛")) return Arrays.asList("友善", "贪吃", "亲人");
        if (n.contains("拉布拉多")) return Arrays.asList("活泼", "服从", "亲人");
        if (n.contains("柯基")) return Arrays.asList("机灵", "粘人", "精力旺盛");
        if (n.contains("英短")) return Arrays.asList("温顺", "安静", "适应力强");
        if ("猫".equals(species)) return Arrays.asList("独立", "好奇", "爱干净");
        if ("狗".equals(species)) return Arrays.asList("友善", "活泼", "亲人");
        return Arrays.asList("温和", "好奇", "需观察");
    }

    private String buildPersonality(String breed, String species, List<String> traits) {
        List<String> finalTraits = traits == null || traits.isEmpty() ? resolveTraits(breed, species) : traits;
        String traitText = String.join("、", finalTraits);
        if ("狗".equals(species)) {
            return breed + "通常" + traitText + "，建议每天保持互动和适量运动，训练时可用零食奖励增强配合度。";
        }
        if ("猫".equals(species)) {
            return breed + "通常" + traitText + "，建议提供安静安全的环境，并通过逗猫棒等方式温和互动。";
        }
        return "该宠物特征建议结合人工确认，日常护理可重点观察饮食、精神状态和活动量。";
    }

    private PetVisionRecognitionDto fallback(String filename) {
        PetVisionRecognitionDto dto = new PetVisionRecognitionDto();
        String lower = filename == null ? "" : filename.toLowerCase();
        boolean cat = lower.contains("cat") || lower.contains("猫");
        boolean dog = lower.contains("dog") || lower.contains("狗") || lower.contains("gold") || lower.contains("金毛");
        dto.setSpecies(cat ? "猫" : (dog ? "狗" : "其他"));
        dto.setBreed(lower.contains("gold") || lower.contains("金毛") ? "金毛寻回犬" : (cat ? "家猫" : (dog ? "家犬" : "未知宠物")));
        dto.setColor("需人工确认");
        dto.setSize(cat ? "小型" : (dog ? "中型" : "需人工确认"));
        dto.setAgeEstimate("成年");
        dto.setAge(3);
        dto.setTraits(resolveTraits(dto.getBreed(), dto.getSpecies()));
        dto.setPersonalitySummary(buildPersonality(dto.getBreed(), dto.getSpecies(), dto.getTraits()));
        dto.setConfidence(0.45D);
        dto.setSource("本地兜底识别");
        dto.setRawName(dto.getBreed());
        return dto;
    }
}
