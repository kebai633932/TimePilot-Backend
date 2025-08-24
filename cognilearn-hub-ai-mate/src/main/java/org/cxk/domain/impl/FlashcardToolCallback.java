//package org.cxk.domain.impl;
//
//import org.springframework.ai.tool.ToolCallback;
//import org.springframework.ai.tool.definition.ToolDefinition;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author KJH
// * @description
// * @create 2025/8/24 12:51
// */
//@Component
//public class FlashcardToolCallback implements ToolCallback {
//
//    @Override
//    public String getName() {
//        return "create_flashcards"; // 要和你 function 定义里的 name 一致
//    }
//
//    @Override
//    public Object call(Object arguments) {
//        // arguments 是模型生成的 JSON 参数，SDK 已经帮你解析成 Map/POJO
//        Map<String, Object> argMap = (Map<String, Object>) arguments;
//        List<Map<String, Object>> flashcards = (List<Map<String, Object>>) argMap.get("flashcards");
//
//        List<Map<String, Object>> processed = new ArrayList<>();
//        for (Map<String, Object> card : flashcards) {
//            Long noteId = ((Number) card.get("noteId")).longValue();
//            String question = (String) card.get("question");
//            String answer = (String) card.get("answer");
//
//            // 你可以在这里做验证、修正、甚至调用本地业务逻辑
//            processed.add(Map.of(
//                    "noteId", noteId,
//                    "question", question,
//                    "answer", answer
//            ));
//        }
//
//        // 返回给 SDK，大模型会当作工具调用的结果使用
//        return Map.of("flashcards", processed);
//    }
//
//    @Override
//    public ToolDefinition getToolDefinition() {
//        return null;
//    }
//
//    @Override
//    public String call(String toolInput) {
//        return null;
//    }
//}
//
