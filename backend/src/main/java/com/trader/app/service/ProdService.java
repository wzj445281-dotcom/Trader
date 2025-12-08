package com.trader.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.trader.app.entity.Fav;
import com.trader.app.entity.Prod;
import com.trader.app.mapper.FavMapper;
import com.trader.app.mapper.ProdMapper;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ProdService {

    @Autowired ProdMapper prodMapper;
    @Autowired FavMapper favMapper;

    //  注入 RedisTemplate
    @Autowired RedisTemplate<String, Object> redisTemplate;

    private final String ABSOLUTE_UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";

    // ... uploadImage 方法保持不变 (篇幅原因省略，请保留原代码) ...
    public String uploadImage(MultipartFile file) throws IOException {
        File dir = new File(ABSOLUTE_UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        String orig = file.getOriginalFilename();
        String name = UUID.randomUUID().toString() + (orig == null ? "" : ("_" + orig));
        File dest = new File(dir, name);
        file.transferTo(dest);

        try {
            File thumb = new File(dir, "thumb_" + name);
            Thumbnails.of(dest).size(800, 800).toFile(thumb);
        } catch (Exception e) {}

        return "/uploads/" + name;
    }

    public Prod publish(Prod p, Long uid) {
        if (uid == null) throw new IllegalArgumentException("User not authenticated");
        p.setUserId(uid);
        p.setCreatedAt(LocalDateTime.now());
        if (p.getStock() == null) p.setStock(1);
        p.setStatus("AVAILABLE");
        prodMapper.insert(p);

        // 🔥 发布新商品时，可以选择删除缓存，或者等待缓存自然过期
        // redisTemplate.delete("home:top:prods");
        return p;
    }

    public List<Prod> list(String q, String category) {
        QueryWrapper<Prod> w = new QueryWrapper<>();
        w.eq("status", "AVAILABLE");
        if (q != null && !q.isEmpty()) w.and(x -> x.like("title", q).or().like("descr", q));
        if (category != null && !category.isEmpty()) w.eq("category", category);
        w.orderByDesc("created_at");
        return prodMapper.selectList(w);
    }

    //  带缓存的热门推荐查询
    @SuppressWarnings("unchecked")
    public List<Prod> getTopProds(Integer n) {
        int limit = (n == null ? 6 : n);
        String cacheKey = "home:top:prods:" + limit;

        // 1. 先查 Redis
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
            System.out.println("🔥 Hit Redis Cache: " + cacheKey);
            return (List<Prod>) redisTemplate.opsForValue().get(cacheKey);
        }

        // 2. 缓存没有，查数据库
        System.out.println("⚡️ Query Database: " + cacheKey);
        QueryWrapper<Prod> w = new QueryWrapper<>();
        w.orderByDesc("view_count").last("LIMIT " + limit);
        List<Prod> list = prodMapper.selectList(w);

        // 3. 写入 Redis (设置 10 分钟过期，防止数据一直不更新)
        if (!list.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, list, 10, TimeUnit.MINUTES);
        }

        return list;
    }

    public void fav(Long uid, Long prodId) {
        if (uid == null) throw new IllegalArgumentException("User not authenticated");
        QueryWrapper<Fav> q = new QueryWrapper<>();
        q.eq("user_id", uid).eq("prod_id", prodId);
        if (favMapper.selectOne(q) != null) throw new IllegalArgumentException("Already favorited");
        Fav f = new Fav();
        f.setUserId(uid);
        f.setProdId(prodId);
        favMapper.insert(f);
    }

    public List<Prod> listByDistance(double lat, double lng) {
        List<Prod> list = prodMapper.selectList(new QueryWrapper<Prod>().eq("status", "AVAILABLE"));
        list.sort((a, b) -> {
            double da = distance(lat, lng, a.getLat() == null ? 0.0 : a.getLat(), a.getLng() == null ? 0.0 : a.getLng());
            double db = distance(lat, lng, b.getLat() == null ? 0.0 : b.getLat(), b.getLng() == null ? 0.0 : b.getLng());
            return Double.compare(da, db);
        });
        return list;
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}