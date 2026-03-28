package com.nutriai.service;

import com.nutriai.entity.*;
import com.nutriai.exception.BusinessException;
import com.nutriai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final CommunityLikeRepository likeRepository;
    private final UserFollowRepository followRepository;
    private final UserRepository userRepository;

    // ========== 帖子 ==========

    public Page<CommunityPost> getFeed(String category, int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommunityPost> posts;
        if (category != null && !category.isEmpty() && !"全部".equals(category)) {
            posts = postRepository.findByStatusAndCategoryOrderByIsPinnedDescCreatedAtDesc("ACTIVE", category, pageable);
        } else {
            posts = postRepository.findByStatusOrderByIsPinnedDescCreatedAtDesc("ACTIVE", pageable);
        }
        // 填充当前用户点赞状态
        if (currentUserId != null && !posts.isEmpty()) {
            List<Long> postIds = posts.getContent().stream().map(CommunityPost::getId).collect(Collectors.toList());
            List<CommunityLike> likes = likeRepository.findByUserIdAndTargetTypeAndTargetIdIn(currentUserId, "POST", postIds);
            Set<Long> likedIds = likes.stream().map(CommunityLike::getTargetId).collect(Collectors.toSet());
            posts.getContent().forEach(p -> p.setLiked(likedIds.contains(p.getId())));
        }
        return posts;
    }

    public Page<CommunityPost> getUserPosts(Long userId, int page, int size) {
        return postRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "ACTIVE", PageRequest.of(page, size));
    }

    @Transactional
    public CommunityPost createPost(Long userId, String content, String images, String videoUrl, String category) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        CommunityPost post = CommunityPost.builder()
                .userId(userId)
                .username(user.getNickname() != null ? user.getNickname() : user.getUsername())
                .avatarUrl(user.getAvatar())
                .content(content)
                .images(images)
                .videoUrl(videoUrl)
                .category(category != null ? category : "饮食打卡")
                .build();
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId, Long userId, boolean isAdmin) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        if (!isAdmin && !post.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该帖子");
        }
        post.setStatus("DELETED");
        postRepository.save(post);
    }

    public CommunityPost getPostDetail(Long postId, Long currentUserId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        if (!"ACTIVE".equals(post.getStatus())) throw new BusinessException("帖子已删除");
        if (currentUserId != null) {
            post.setLiked(likeRepository.existsByUserIdAndTargetTypeAndTargetId(currentUserId, "POST", postId));
        }
        return post;
    }

    // ========== 评论 ==========

    public Page<CommunityComment> getComments(Long postId, int page, int size, Long currentUserId) {
        Page<CommunityComment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId, PageRequest.of(page, size));
        if (currentUserId != null && !comments.isEmpty()) {
            List<Long> ids = comments.getContent().stream().map(CommunityComment::getId).collect(Collectors.toList());
            List<CommunityLike> likes = likeRepository.findByUserIdAndTargetTypeAndTargetIdIn(currentUserId, "COMMENT", ids);
            Set<Long> likedIds = likes.stream().map(CommunityLike::getTargetId).collect(Collectors.toSet());
            comments.getContent().forEach(c -> c.setLiked(likedIds.contains(c.getId())));
        }
        return comments;
    }

    @Transactional
    public CommunityComment addComment(Long postId, Long userId, String content, Long parentId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        String replyToUsername = null;
        if (parentId != null) {
            CommunityComment parent = commentRepository.findById(parentId).orElse(null);
            if (parent != null) replyToUsername = parent.getUsername();
        }

        CommunityComment comment = CommunityComment.builder()
                .postId(postId)
                .userId(userId)
                .username(user.getNickname() != null ? user.getNickname() : user.getUsername())
                .avatarUrl(user.getAvatar())
                .content(content)
                .parentId(parentId)
                .replyToUsername(replyToUsername)
                .build();
        comment = commentRepository.save(comment);
        postRepository.updateCommentsCount(postId, 1);
        return comment;
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
        CommunityComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该评论");
        }
        commentRepository.delete(comment);
        postRepository.updateCommentsCount(comment.getPostId(), -1);
    }

    // ========== 点赞 ==========

    @Transactional
    public boolean toggleLike(Long userId, String targetType, Long targetId) {
        Optional<CommunityLike> existing = likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            if ("POST".equals(targetType)) postRepository.updateLikesCount(targetId, -1);
            else commentRepository.updateLikesCount(targetId, -1);
            return false; // unliked
        } else {
            likeRepository.save(CommunityLike.builder()
                    .userId(userId).targetType(targetType).targetId(targetId).build());
            if ("POST".equals(targetType)) postRepository.updateLikesCount(targetId, 1);
            else commentRepository.updateLikesCount(targetId, 1);
            return true; // liked
        }
    }

    // ========== 关注 ==========

    @Transactional
    public boolean toggleFollow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) throw new BusinessException("不能关注自己");
        Optional<UserFollow> existing = followRepository.findByFollowerIdAndFollowingId(followerId, followingId);
        if (existing.isPresent()) {
            followRepository.delete(existing.get());
            return false;
        } else {
            followRepository.save(UserFollow.builder()
                    .followerId(followerId).followingId(followingId).build());
            return true;
        }
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    public Map<String, Long> getFollowStats(Long userId) {
        return Map.of(
                "following", followRepository.countByFollowerId(userId),
                "followers", followRepository.countByFollowingId(userId)
        );
    }

    // ========== 管理 ==========

    public Page<CommunityPost> adminGetPosts(String status, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null && !status.isEmpty()) {
            if (category != null && !category.isEmpty()) {
                return postRepository.findByStatusAndCategoryOrderByIsPinnedDescCreatedAtDesc(status, category, pageable);
            }
            return postRepository.findByStatusOrderByIsPinnedDescCreatedAtDesc(status, pageable);
        }
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional
    public void adminTogglePin(Long postId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        post.setIsPinned(!post.getIsPinned());
        postRepository.save(post);
    }

    @Transactional
    public void adminUpdateStatus(Long postId, String status) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        post.setStatus(status);
        postRepository.save(post);
    }

    public Map<String, Long> getStats() {
        return Map.of(
                "total", postRepository.count(),
                "active", postRepository.countByStatus("ACTIVE"),
                "deleted", postRepository.countByStatus("DELETED")
        );
    }
}
