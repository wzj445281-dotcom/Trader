package com.trader.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.trader.app.entity.Fav;
import com.trader.app.entity.Prod;
import com.trader.app.mapper.FavMapper;
import com.trader.app.mapper.ProdMapper;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProdService {

    @Autowired ProdMapper prodMapper;
    @Autowired FavMapper favMapper;

    // 尽管有配置，但我们通过 System.getProperty("user.dir") 强制获取项目的绝对路径
    // 这样能解决 Windows 下的相对路径解析问题
    private final String ABSOLUTE_UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";


    public String uploadImage(MultipartFile file) throws IOException {
        File dir = new File(ABSOLUTE_UPLOAD_DIR);

        // 检查并创建目录
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create upload directory: " + ABSOLUTE_UPLOAD_DIR);
            }
        }

        // 检查目录是否可写
        if (!dir.canWrite()) {
            throw new IOException("Upload directory is not writable: " + ABSOLUTE_UPLOAD_DIR);
        }

        String orig = file.getOriginalFilename();
        String name = UUID.randomUUID().toString() + (orig == null ? "" : ("_" + orig));
        File dest = new File(dir, name);

        // 打印绝对路径，用于确认是否正确
        System.out.println("Uploading file to absolute path: " + dest.getAbsolutePath());

        try {
            // 🔥 核心修复点：将文件转移到我们明确指定的绝对路径
            file.transferTo(dest);
        } catch (IllegalStateException e) {
            // 捕获可能的文件已移动的异常
            throw new IOException("File already moved or temporary file access error.", e);
        } catch (IOException e) {
            // 捕获找不到路径的异常
            throw new IOException("Failed to save file to " + dest.getAbsolutePath(), e);
        }

        // Try creating thumbnail
        try {
            File thumb = new File(dir, "thumb_" + name);
            Thumbnails.of(dest).size(800, 800).toFile(thumb);
        } catch (Exception e) {
            System.err.println("Thumbnail generation failed: " + e.getMessage());
        }

        // 数据库存储的仍然是相对访问路径
        return "/uploads/" + name;
    }

    public Prod publish(Prod p, Long uid) {
        if (uid == null) throw new IllegalArgumentException("User not authenticated");
        p.setUserId(uid);
        p.setCreatedAt(LocalDateTime.now());
        if (p.getStock() == null) p.setStock(1);
        p.setStatus("AVAILABLE");
        prodMapper.insert(p);
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

    public void fav(Long uid, Long prodId) {
        if (uid == null) throw new IllegalArgumentException("User not authenticated");

        QueryWrapper<Fav> q = new QueryWrapper<>();
        q.eq("user_id", uid).eq("prod_id", prodId);

        if (favMapper.selectOne(q) != null) {
            throw new IllegalArgumentException("Already favorited");
        }

        Fav f = new Fav();
        f.setUserId(uid);
        f.setProdId(prodId);
        favMapper.insert(f);
    }

    public List<Prod> listByDistance(double lat, double lng) {
        // TODO: In production, use PostGIS or MySQL Spatial Indexes for bounding box query first
        // SELECT * FROM prod WHERE lat BETWEEN ... AND ...

        List<Prod> list = prodMapper.selectList(new QueryWrapper<Prod>().eq("status", "AVAILABLE"));
        list.sort((a, b) -> {
            double da = distance(lat, lng, a.getLat() == null ? 0.0 : a.getLat(), a.getLng() == null ? 0.0 : a.getLng());
            double db = distance(lat, lng, b.getLat() == null ? 0.0 : b.getLat(), b.getLng() == null ? 0.0 : b.getLng());
            return Double.compare(da, db);
        });
        return list;
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}