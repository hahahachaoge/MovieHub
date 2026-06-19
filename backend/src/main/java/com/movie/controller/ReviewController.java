package com.movie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.movie.common.Result;
import com.movie.entity.Review;
import com.movie.mapper.ReviewMapper;
import com.movie.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/movie/{movieId}")
    public Result<List<Review>> getReviews(@PathVariable Long movieId) {
        List<Review> reviews = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getMovieId, movieId)
                        .orderByDesc(Review::getCreateTime)
        );
        // 填充用户名
        for (Review review : reviews) {
            if (review.getUserId() != null) {
                var user = userMapper.selectById(review.getUserId());
                if (user != null) {
                    review.setUsername(user.getUsername());
                    review.setUserAvatar(user.getAvatar());
                }
            }
        }
        return Result.success(reviews);
    }

    @PostMapping("/submit")
    public Result<Void> submitReview(@RequestAttribute("userId") Long userId,
                                      @RequestBody Review review) {
        review.setUserId(userId);
        review.setId(null); // 确保是新增
        reviewMapper.insert(review);
        return Result.success();
    }
}
