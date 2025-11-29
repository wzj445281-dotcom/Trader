package com.trader.app.controller;
import com.trader.app.entity.Prod;
import com.trader.app.entity.Fav;
import com.trader.app.mapper.ProdMapper;
import com.trader.app.mapper.FavMapper;
import com.trader.app.util.Result;
import com.trader.app.util.JwtUtil;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prod")
public class ProdCtrl {
    @Autowired ProdMapper prodMapper;
    @Autowired FavMapper favMapper;
    @Value("${app.upload-dir}") private String uploadDir;

    @PostMapping("/uploadImg")
    public Result<String> uploadImg(@RequestParam("file") MultipartFile file) throws IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        String orig = file.getOriginalFilename();
        String name = UUID.randomUUID().toString() + (orig==null?"":("_"+orig));
        File dest = new File(dir, name);
        // save original
        file.transferTo(dest);
        // create compressed thumbnail (max 800x800)
        try {
            File thumb = new File(dir, "thumb_" + name);
            Thumbnails.of(dest).size(800,800).toFile(thumb);
        } catch (Exception e){ /* ignore */ }
        return Result.ok("/uploads/" + name);
    }

    @PostMapping("/publish")
    public Result<Prod> publish(@RequestHeader(value="Authorization", required=false) String auth, @RequestBody Prod p){
        Long uid = null; try { Object prin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal(); if (prin instanceof Long) uid = (Long)prin; else if (prin!=null) uid = Long.valueOf(prin.toString()); } catch(Exception e){}
        if (uid==null && p.getUserId()==null) return Result.fail("unauth");
        if (p.getUserId()==null) p.setUserId(uid);
        p.setCreatedAt(LocalDateTime.now());
        p.setStatus("AVAILABLE");
        prodMapper.insert(p);
        return Result.ok(p);
    }

    @GetMapping("/list")
    public Result<List<Prod>> list(@RequestParam(value="q", required=false) String q,
                                   @RequestParam(value="category", required=false) String category){
        QueryWrapper<Prod> w = new QueryWrapper<>();
        w.eq("status","AVAILABLE");
        if (q!=null && !q.isEmpty()) w.and(x->x.like("title", q).or().like("descr", q));
        if (category!=null && !category.isEmpty()) w.eq("category", category);
        w.orderByDesc("created_at");
        List<Prod> list = prodMapper.selectList(w);
        return Result.ok(list);
    }

    @GetMapping("/{id}")
    public Result<Prod> detail(@PathVariable Long id){
        Prod p = prodMapper.selectById(id);
        if (p==null) return Result.fail("not found");
        return Result.ok(p);
    }

    @PostMapping("/fav")
    public Result<String> fav(@RequestHeader(value="Authorization", required=false) String auth, @RequestBody Fav f){
        Long uid = null; try { Object prin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal(); if (prin instanceof Long) uid = (Long)prin; else if (prin!=null) uid = Long.valueOf(prin.toString()); } catch(Exception e){}
        if (uid==null && f.getUserId()==null) return Result.fail("unauth");
        if (f.getUserId()==null) f.setUserId(uid);
        QueryWrapper<Fav> q = new QueryWrapper<>();
        q.eq("user_id", f.getUserId()).eq("prod_id", f.getProdId());
        if (favMapper.selectOne(q)!=null) return Result.fail("already fav");
        favMapper.insert(f);
        return Result.ok("fav ok");
    }

    @GetMapping("/favs/{userId}")
    public Result<List<Prod>> favs(@PathVariable Long userId){
        QueryWrapper<Fav> q = new QueryWrapper<>();
        q.eq("user_id", userId);
        List<Fav> fs = favMapper.selectList(q);
        List<Long> ids = fs.stream().map(Fav::getProdId).collect(Collectors.toList());
        if (ids.isEmpty()) return Result.ok(Collections.emptyList());
        List<Prod> ps = prodMapper.selectBatchIds(ids);
        return Result.ok(ps);
    }

// increment view count and return product
@PostMapping("/view/{id}")
public Result<Prod> view(@PathVariable Long id){
    Prod p = prodMapper.selectById(id);
    if (p==null) return Result.fail("not found");
    if (p.getViewCount()==null) p.setViewCount(0);
    p.setViewCount(p.getViewCount()+1);
    prodMapper.updateById(p);
    return Result.ok(p);
}

// recommend top N by viewCount
@GetMapping("/recommend/top")
public Result<List<Prod>> recommendTop(@RequestParam(value="n", required=false) Integer n){
    if (n==null) n=6;
    com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Prod> w = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
    w.orderByDesc("view_count").last("LIMIT " + n);
    List<Prod> list = prodMapper.selectList(w);
    return Result.ok(list);
}

// list by distance: requires lat,lng param
@GetMapping("/listByDistance")
public Result<List<Prod>> listByDistance(@RequestParam double lat, @RequestParam double lng, @RequestParam(value="q", required=false) String q){
    List<Prod> list = prodMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Prod>().eq("status","AVAILABLE"));
    // compute Haversine distance
    list.sort((a,b)->{
        double da = distance(lat,lng, a.getLat()==null?0.0:a.getLat(), a.getLng()==null?0.0:a.getLng());
        double db = distance(lat,lng, b.getLat()==null?0.0:b.getLat(), b.getLng()==null?0.0:b.getLng());
        return Double.compare(da, db);
    });
    return Result.ok(list);
}

private static double distance(double lat1, double lon1, double lat2, double lon2) {
    double R = 6371; // km
    double dLat = Math.toRadians(lat2-lat1);
    double dLon = Math.toRadians(lon2-lon1);
    double a = Math.sin(dLat/2)*Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.sin(dLon/2)*Math.sin(dLon/2);
    double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R*c;
}

// comments
@Autowired
private com.trader.app.mapper.CommentMapper commentMapper;

@PostMapping("/comment")
public Result<String> comment(@RequestBody com.trader.app.entity.Comment cm){
    cm.setCreatedAt(System.currentTimeMillis());
    commentMapper.insert(cm);
    return Result.ok("ok");
}

@GetMapping("/comments/{prodId}")
public Result<List<com.trader.app.entity.Comment>> comments(@PathVariable Long prodId){
    List<com.trader.app.entity.Comment> cs = commentMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.trader.app.entity.Comment>().eq("prod_id", prodId));
    return Result.ok(cs);
}

// report
@Autowired
private com.trader.app.mapper.ReportMapper reportMapper;

@PostMapping("/report")
public Result<String> report(@RequestBody com.trader.app.entity.Report r){
    r.setCreatedAt(System.currentTimeMillis());
    r.setStatus("OPEN");
    reportMapper.insert(r);
    return Result.ok("reported");
}

// notifications
@Autowired
private com.trader.app.mapper.NotificationMapper notificationMapper;

@GetMapping("/notifications/{userId}")
public Result<List<com.trader.app.entity.Notification>> notifications(@PathVariable Long userId){
    List<com.trader.app.entity.Notification> ns = notificationMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.trader.app.entity.Notification>().eq("user_id", userId));
    return Result.ok(ns);
}

@PostMapping("/notify")
public Result<String> notifyUser(@RequestBody com.trader.app.entity.Notification n){
    n.setCreatedAt(System.currentTimeMillis());
    if (n.getRead()==null) n.setRead(false);
    notificationMapper.insert(n);
    return Result.ok("ok");
}

// cart
@Autowired
private com.trader.app.mapper.CartItemMapper cartItemMapper;

@PostMapping("/cart/add")
public Result<String> addToCart(@RequestBody com.trader.app.entity.CartItem c){
    cartItemMapper.insert(c);
    return Result.ok("ok");
}

@GetMapping("/cart/{userId}")
public Result<List<com.trader.app.entity.CartItem>> getCart(@PathVariable Long userId){
    List<com.trader.app.entity.CartItem> cs = cartItemMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.trader.app.entity.CartItem>().eq("user_id", userId));
    return Result.ok(cs);
}

// orders
@Autowired
private com.trader.app.mapper.OrderMapper orderMapper;

@PostMapping("/order/create")
public Result<com.trader.app.entity.OrderEntity> createOrder(@RequestBody com.trader.app.entity.OrderEntity o){
    o.setCreatedAt(System.currentTimeMillis());
    o.setStatus("CREATED");
    orderMapper.insert(o);
    // create notification to seller
    com.trader.app.entity.Notification n = new com.trader.app.entity.Notification();
    n.setUserId(o.getSellerId()); n.setTitle("New order"); n.setBody("Order for prod " + o.getProdId()); n.setCreatedAt(System.currentTimeMillis()); n.setRead(false);
    notificationMapper.insert(n);
    return Result.ok(o);
}

@PostMapping("/order/updateStatus")
public Result<String> updateOrderStatus(@RequestBody com.trader.app.entity.OrderEntity o){
    com.trader.app.entity.OrderEntity ex = orderMapper.selectById(o.getId());
    if (ex==null) return Result.fail("no order");
    ex.setStatus(o.getStatus());
    orderMapper.updateById(ex);
    return Result.ok("ok");
}

// price history
@Autowired
private com.trader.app.mapper.PriceHistoryMapper priceHistoryMapper;

@PostMapping("/price/record")
public Result<String> recordPrice(@RequestBody com.trader.app.entity.PriceHistory ph){
    ph.setChangedAt(System.currentTimeMillis());
    priceHistoryMapper.insert(ph);
    return Result.ok("ok");
}

@GetMapping("/price/history/{prodId}")
public Result<List<com.trader.app.entity.PriceHistory>> priceHistory(@PathVariable Long prodId){
    List<com.trader.app.entity.PriceHistory> hs = priceHistoryMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.trader.app.entity.PriceHistory>().eq("prod_id", prodId));
    return Result.ok(hs);
}

// personal pages
@GetMapping("/my/products/{userId}")
public Result<List<Prod>> myProducts(@PathVariable Long userId){
    List<Prod> list = prodMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Prod>().eq("user_id", userId));
    return Result.ok(list);
}

@GetMapping("/my/orders/{userId}")
public Result<List<com.trader.app.entity.OrderEntity>> myOrders(@PathVariable Long userId){
    List<com.trader.app.entity.OrderEntity> os = orderMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.trader.app.entity.OrderEntity>().eq("buyer_id", userId).or().eq("seller_id", userId));
    return Result.ok(os);
}

@GetMapping("/my/favorites/{userId}")
public Result<List<Prod>> myFavorites(@PathVariable Long userId){
    QueryWrapper<com.trader.app.entity.Fav> q = new QueryWrapper<>();
    q.eq("user_id", userId);
    List<com.trader.app.entity.Fav> favs = favMapper.selectList(q);
    List<Long> pids = favs.stream().map(com.trader.app.entity.Fav::getProdId).collect(java.util.stream.Collectors.toList());
    if (pids.isEmpty()) return Result.ok(java.util.Collections.emptyList());
    List<Prod> list = prodMapper.selectBatchIds(pids);
    return Result.ok(list);
}

}
