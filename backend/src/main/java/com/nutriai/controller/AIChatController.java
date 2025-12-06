package com.nutriai.controller;

import com.nutriai.common.ApiResponse;
import com.nutriai.dto.AIChatFavoriteDTO;
import com.nutriai.dto.AIChatHistoryDTO;
import com.nutriai.service.AIChatService;
import com.nutriai.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI聊天Controller
 */
@Slf4j
@RestController
@RequestMapping("/ai-chat")
@RequiredArgsConstructor
public class AIChatController {
    
    private final AIChatService aiChatService;
    private final JwtUtil jwtUtil;
    
    /**
     * 保存或更新聊天历史
     */
    @PostMapping("/history")
    public ResponseEntity<ApiResponse<AIChatHistoryDTO>> saveHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            String title = (String) request.get("title");
            String messages = (String) request.get("messages");
            Long historyId = request.get("id") != null ? 
                    Long.parseLong(request.get("id").toString()) : null;
            
            AIChatHistoryDTO history = aiChatService.saveHistory(userId, historyId, title, messages);
            
            return ResponseEntity.ok(ApiResponse.success("保存成功", history));
            
        } catch (Exception e) {
            log.error("保存聊天历史失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "保存失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取历史记录列表（分页）
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<AIChatHistoryDTO>>> getHistoryList(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            Page<AIChatHistoryDTO> history = aiChatService.getHistoryList(userId, page, size);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", history));
            
        } catch (Exception e) {
            log.error("获取历史记录失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有历史记录
     */
    @GetMapping("/history/all")
    public ResponseEntity<ApiResponse<List<AIChatHistoryDTO>>> getAllHistory(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            List<AIChatHistoryDTO> history = aiChatService.getAllHistory(userId);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", history));
            
        } catch (Exception e) {
            log.error("获取所有历史记录失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取历史记录详情
     */
    @GetMapping("/history/{id}")
    public ResponseEntity<ApiResponse<AIChatHistoryDTO>> getHistoryDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            AIChatHistoryDTO history = aiChatService.getHistoryDetail(id, userId);
            
            if (history == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, "历史记录不存在"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", history));
            
        } catch (Exception e) {
            log.error("获取历史记录详情失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除历史记录
     */
    @DeleteMapping("/history/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHistory(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            boolean deleted = aiChatService.deleteHistory(id, userId);
            
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("删除成功", null));
            } else {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, "历史记录不存在"));
            }
            
        } catch (Exception e) {
            log.error("删除历史记录失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 添加收藏
     */
    @PostMapping("/favorite")
    public ResponseEntity<ApiResponse<AIChatFavoriteDTO>> addFavorite(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            String messageContent = request.get("messageContent");
            String messageRole = request.get("messageRole");
            
            AIChatFavoriteDTO favorite = aiChatService.addFavorite(userId, messageContent, messageRole);
            
            return ResponseEntity.ok(ApiResponse.success("收藏成功", favorite));
            
        } catch (Exception e) {
            log.error("添加收藏失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "收藏失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取收藏列表（分页）
     */
    @GetMapping("/favorite")
    public ResponseEntity<ApiResponse<Page<AIChatFavoriteDTO>>> getFavoriteList(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            Page<AIChatFavoriteDTO> favorites = aiChatService.getFavoriteList(userId, page, size);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", favorites));
            
        } catch (Exception e) {
            log.error("获取收藏列表失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有收藏
     */
    @GetMapping("/favorite/all")
    public ResponseEntity<ApiResponse<List<AIChatFavoriteDTO>>> getAllFavorites(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            List<AIChatFavoriteDTO> favorites = aiChatService.getAllFavorites(userId);
            
            return ResponseEntity.ok(ApiResponse.success("获取成功", favorites));
            
        } catch (Exception e) {
            log.error("获取所有收藏失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "获取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除收藏
     */
    @DeleteMapping("/favorite/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFavorite(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            boolean deleted = aiChatService.deleteFavorite(id, userId);
            
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("删除成功", null));
            } else {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, "收藏不存在"));
            }
            
        } catch (Exception e) {
            log.error("删除收藏失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "删除失败: " + e.getMessage()));
        }
    }
}
