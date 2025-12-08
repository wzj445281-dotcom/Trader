package com.trader.app.controller;

import com.trader.app.entity.*;
import com.trader.app.service.ProdService;
import com.trader.app.util.Result;
import com.trader.app.mapper.ProdMapper;
import com.trader.app.mapper.FavMapper;
import com.trader.app.mapper.NotificationMapper;
import com.trader.app.mapper.CommentMapper;
import com.trader.app.mapper.ReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prod")
public class ProdCtrl {

    @Autowired private ProdService prodService;

    @Autowired private ProdMapper prodMapper;
    @Autowired private FavMapper favMapper;
    @Autowired private NotificationMapper notificationMapper;
    @Autowired private CommentMapper commentMapper;
    @Autowired private ReportMapper reportMapper;

    @PostMapping("/uploadImg")
    public Result<String> uploadImg(@RequestParam("file") MultipartFile file) throws IOException {
        return Result.ok(prodService.uploadImage(file));
    }

    @PostMapping("/publish")
    public Result<Prod> publish(@RequestBody Prod p){
        try {
            // 增强错误捕获：如果发布失败，将抛出异常并返回错误信息
            return Result.ok(prodService.publish(p, getCurrentUserId()));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Product publish failed: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<Prod>> list(@RequestParam(value="q", required=false) String q,
                                   @RequestParam(value="category", required=false) String category){
        return Result.ok(prodService.list(q, category));
    }

    // 获取“我发布的”商品列表接口
    @GetMapping("/my")
    public Result<List<Prod>> myProds() {
        Long uid = getCurrentUserId();
        if (uid == null) return Result.fail("Not logged in");

        QueryWrapper<Prod> q = new QueryWrapper<>();
        q.eq("user_id", uid);
        q.orderByDesc("created_at");

        return Result.ok(prodMapper.selectList(q));
    }

    // 🔥🔥 补全功能 1：编辑商品
    @PostMapping("/update")
    public Result<Prod> update(@RequestBody Prod p) {
        Long uid = getCurrentUserId();
        if (uid == null) return Result.fail("未登录");
        if (p.getId() == null) return Result.fail("商品ID缺失");

        Prod exist = prodMapper.selectById(p.getId());
        if (exist == null) return Result.fail("商品不存在");

        // 权限校验：只能修改自己的商品
        if (!exist.getUserId().equals(uid)) {
            return Result.fail("无权修改他人的商品");
        }

        // 状态校验：如果商品已售出或有人正在下单(LOCKED)，则不允许修改
        if ("SOLD".equals(exist.getStatus()) || "LOCKED".equals(exist.getStatus())) {
            return Result.fail("商品处于交易中或已售出，无法修改信息");
        }

        // 更新允许修改的字段
        if (p.getTitle() != null) exist.setTitle(p.getTitle());
        if (p.getDescr() != null) exist.setDescr(p.getDescr());
        if (p.getPrice() != null) exist.setPrice(p.getPrice());
        if (p.getImages() != null) exist.setImages(p.getImages());
        if (p.getCategory() != null) exist.setCategory(p.getCategory());
        if (p.getStock() != null) exist.setStock(p.getStock());

        prodMapper.updateById(exist);
        return Result.ok(exist);
    }

    // 🔥🔥 补全功能 2：删除商品
    @DeleteMapping("/{id}")
    public Result<String> deleteProd(@PathVariable Long id) {
        Long uid = getCurrentUserId();
        if (uid == null) return Result.fail("未登录");

        Prod exist = prodMapper.selectById(id);
        if (exist == null) return Result.fail("商品不存在");

        // 权限校验
        if (!exist.getUserId().equals(uid)) {
            return Result.fail("无权删除他人的商品");
        }

        // 状态校验
        if ("SOLD".equals(exist.getStatus()) || "LOCKED".equals(exist.getStatus())) {
            return Result.fail("商品处于交易中或已售出，无法删除");
        }

        prodMapper.deleteById(id);
        return Result.ok("商品已删除");
    }

    @GetMapping("/{id}")
    public Result<Prod> detail(@PathVariable String id){
        Long prodId = null;
        try {
            prodId = Long.valueOf(id);
        } catch (NumberFormatException e) {
            return Result.fail("Invalid product ID format");
        }

        Prod p = prodMapper.selectById(prodId);
        if (p == null) return Result.fail("Product not found");
        return Result.ok(p);
    }

    @PostMapping("/fav")
    public Result<String> fav(@RequestBody Fav f){
        prodService.fav(getCurrentUserId(), f.getProdId());
        return Result.ok("Favorited");
    }

    @GetMapping("/favs")
    public Result<List<Prod>> myFavs(){
        Long userId = getCurrentUserId();
        if (userId == null) return Result.fail("Not logged in");

        List<Fav> fs = favMapper.selectList(new QueryWrapper<Fav>().eq("user_id", userId));
        if (fs.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        List<Long> ids = fs.stream().map(Fav::getProdId).collect(Collectors.toList());
        return Result.ok(prodMapper.selectBatchIds(ids));
    }

    // 地理位置搜索
    @GetMapping("/listByDistance")
    public Result<List<Prod>> listByDistance(@RequestParam double lat, @RequestParam double lng){
        return Result.ok(prodService.listByDistance(lat, lng));
    }

    // 通知相关
    @GetMapping("/notifications/{userId}")
    public Result<List<Notification>> getNotes(@PathVariable Long userId){
        Long currentUid = getCurrentUserId();
        if (currentUid == null || !currentUid.equals(userId)) {
            return Result.fail("Unauthorized access to notifications");
        }
        QueryWrapper<Notification> w = new QueryWrapper<>();
        w.eq("user_id", userId).orderByDesc("created_at");
        return Result.ok(notificationMapper.selectList(w));
    }

    // 评论相关
    @GetMapping("/comments/{prodId}")
    public Result<List<Comment>> getComments(@PathVariable Long prodId){
        QueryWrapper<Comment> w = new QueryWrapper<>();
        w.eq("prod_id", prodId).orderByDesc("created_at");
        return Result.ok(commentMapper.selectList(w));
    }

    @PostMapping("/comment")
    public Result<Comment> postComment(@RequestBody Comment c){
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) return Result.fail("User not logged in");

        c.setUserId(currentUserId);
        c.setCreatedAt(System.currentTimeMillis());
        if (c.getRating() == null) c.setRating(5);

        commentMapper.insert(c);
        return Result.ok(c);
    }

    // 举报 API
    @PostMapping("/report")
    public Result<String> report(@RequestBody Report r){
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) return Result.fail("User not logged in");

        if (r.getProdId() == null || r.getReason() == null || r.getReason().isBlank()) {
            return Result.fail("Missing product ID or reason");
        }

        r.setReporterId(currentUserId);
        r.setCreatedAt(System.currentTimeMillis());
        r.setStatus("OPEN");

        reportMapper.insert(r);
        return Result.ok("Report submitted successfully");
    }

    @PostMapping("/view/{id}")
    public Result<Prod> view(@PathVariable Long id){
        Prod p = prodMapper.selectById(id);
        if (p!=null) {
            p.setViewCount(p.getViewCount() == null ? 1 : p.getViewCount() + 1);
            prodMapper.updateById(p);
        }
        return Result.ok(p);
    }

    @GetMapping("/recommend/top")
    public Result<List<Prod>> recommendTop(@RequestParam(value="n", required=false) Integer n){
        QueryWrapper<Prod> w = new QueryWrapper<>();
        w.orderByDesc("view_count").last("LIMIT " + (n==null?6:n));
        return Result.ok(prodMapper.selectList(w));
    }

    private Long getCurrentUserId() {
        try {
            Object prin = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (prin instanceof Long) return (Long) prin;
            if (prin != null) return Long.valueOf(prin.toString());
        } catch(Exception e){}
        return null;
    }
}