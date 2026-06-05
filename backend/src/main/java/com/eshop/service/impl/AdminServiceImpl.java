package com.eshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eshop.common.BusinessException;
import com.eshop.common.PageResult;
import com.eshop.entity.*;
import com.eshop.mapper.*;
import com.eshop.service.AdminService;
import com.eshop.vo.StatisticsVO;
import com.eshop.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final RefundMapper refundMapper;

    @Override
    public PageResult<UserVO> getUsers(String keyword, String role, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getRole, "USER");

        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> p = new Page<>(page, size);
        Page<User> result = userMapper.selectPage(p, wrapper);

        List<UserVO> records = result.getRecords().stream().map(u -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(u, vo);
            return vo;
        }).toList();

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null || !"USER".equals(user.getRole())) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public PageResult<UserVO> getMerchants(String keyword, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getRole, "MERCHANT");

        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getStoreName, keyword));
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> p = new Page<>(page, size);
        Page<User> result = userMapper.selectPage(p, wrapper);

        List<UserVO> records = result.getRecords().stream().map(u -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(u, vo);
            return vo;
        }).toList();

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public void updateMerchantStatus(Long merchantId, Integer status) {
        User user = userMapper.selectById(merchantId);
        if (user == null || !"MERCHANT".equals(user.getRole())) {
            throw new BusinessException("商家不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public StatisticsVO getStats() {
        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, "USER"));
        long totalMerchants = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, "MERCHANT"));
        long totalProducts = productMapper.selectCount(null);

        Map<String, Object> stats = orderMapper.selectStats(null);
        long totalOrders = stats.get("total_orders") != null ? ((Number) stats.get("total_orders")).longValue() : 0;
        BigDecimal totalSales = stats.get("total_sales") != null ? new BigDecimal(stats.get("total_sales").toString()) : BigDecimal.ZERO;
        long pendingOrders = stats.get("pending_delivery") != null ? ((Number) stats.get("pending_delivery")).longValue() : 0;
        long pendingRefunds = stats.get("refunding") != null ? ((Number) stats.get("refunding")).longValue() : 0;

        // 今日数据
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long todayOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().ge(Order::getCreatedAt, todayStart)
        );

        return StatisticsVO.builder()
                .totalUsers(totalUsers)
                .totalMerchants(totalMerchants)
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalSales(totalSales)
                .todayOrders(todayOrders)
                .pendingOrders(pendingOrders)
                .pendingRefunds(pendingRefunds)
                .build();
    }

    @Override
    public Object getChartData(String type) {
        // 返回最近7天的订单数据
        List<Map<String, Object>> chartData = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

            long count = orderMapper.selectCount(
                    new LambdaQueryWrapper<Order>()
                            .ge(Order::getCreatedAt, start)
                            .le(Order::getCreatedAt, end)
            );

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.toString());
            item.put("count", count);
            chartData.add(item);
        }
        return chartData;
    }
}
