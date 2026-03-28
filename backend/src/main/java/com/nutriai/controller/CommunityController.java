package com.nutriai.controller;

import com.nutriai.common.ApiResponse;
import com.nutriai.entity.CommunityComment;
import com.nutriai.entity.CommunityPost;
import com.nutriai.service.CommunityService;
import com.nutriai.service.OssService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final OssService ossService;

    // ========== 帖子 Feed ==========

    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<Page<CommunityPost>>> getFeed(
            HttpServletRequest request,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(ApiResponse.success(communityService.getFeed(category, page, size, userId)));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<CommunityPost>> getPostDetail(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(ApiResponse.success(communityService.getPostDetail(id, userId)));
    }

    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<ApiResponse<Page<CommunityPost>>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(communityService.getUserPosts(userId, page, size)));
    }

    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<CommunityPost>> createPost(
            HttpServletRequest request,
            @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        CommunityPost post = communityService.createPost(
                userId,
                body.get("content"),
                body.get("images"),
                body.get("videoUrl"),
                body.get("category")
        );
        return ResponseEntity.ok(ApiResponse.success("发布成功", post));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        communityService.deletePost(id, userId, false);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    // ========== 图片/视频上传 ==========

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadMedia(
            @RequestParam("file") MultipartFile file) {
        String url = ossService.uploadCommunityMedia(file);
        return ResponseEntity.ok(ApiResponse.success("上传成功", url));
    }

    // ========== 评论 ==========

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Page<CommunityComment>>> getComments(
            HttpServletRequest request,
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(ApiResponse.success(communityService.getComments(postId, page, size, userId)));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommunityComment>> addComment(
            HttpServletRequest request,
            @PathVariable Long postId,
            @RequestBody Map<String, Object> body) {
        Long userId = (Long) request.getAttribute("userId");
        String content = (String) body.get("content");
        Long parentId = body.get("parentId") != null ? Long.valueOf(body.get("parentId").toString()) : null;
        CommunityComment comment = communityService.addComment(postId, userId, content, parentId);
        return ResponseEntity.ok(ApiResponse.success("评论成功", comment));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        communityService.deleteComment(id, userId, false);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    // ========== 点赞 ==========

    @PostMapping("/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleLike(
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) {
        Long userId = (Long) request.getAttribute("userId");
        String targetType = (String) body.get("targetType");
        Long targetId = Long.valueOf(body.get("targetId").toString());
        boolean liked = communityService.toggleLike(userId, targetType, targetId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("liked", liked)));
    }

    // ========== 关注 ==========

    @PostMapping("/follow/{targetUserId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleFollow(
            HttpServletRequest request,
            @PathVariable Long targetUserId) {
        Long userId = (Long) request.getAttribute("userId");
        boolean followed = communityService.toggleFollow(userId, targetUserId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("followed", followed)));
    }

    @GetMapping("/follow/{targetUserId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFollowStatus(
            HttpServletRequest request,
            @PathVariable Long targetUserId) {
        Long userId = (Long) request.getAttribute("userId");
        boolean isFollowing = communityService.isFollowing(userId, targetUserId);
        Map<String, Long> stats = communityService.getFollowStats(targetUserId);
        Map<String, Object> result = new java.util.HashMap<>(stats);
        result.put("isFollowing", isFollowing);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ========== 管理后台 ==========

    @GetMapping("/admin/posts")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<CommunityPost>>> adminGetPosts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(communityService.adminGetPosts(status, category, page, size)));
    }

    @PutMapping("/admin/posts/{id}/pin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminTogglePin(@PathVariable Long id) {
        communityService.adminTogglePin(id);
        return ResponseEntity.ok(ApiResponse.success("操作成功", null));
    }

    @PutMapping("/admin/posts/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminUpdateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        communityService.adminUpdateStatus(id, body.get("status"));
        return ResponseEntity.ok(ApiResponse.success("操作成功", null));
    }

    @DeleteMapping("/admin/posts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminDeletePost(@PathVariable Long id) {
        communityService.deletePost(id, null, true);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @DeleteMapping("/admin/comments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminDeleteComment(@PathVariable Long id) {
        communityService.deleteComment(id, null, true);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(communityService.getStats()));
    }
}
