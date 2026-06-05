package com.eshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eshop.common.BusinessException;
import com.eshop.config.JwtUtils;
import com.eshop.dto.LoginDTO;
import com.eshop.dto.RegisterDTO;
import com.eshop.entity.User;
import com.eshop.mapper.UserMapper;
import com.eshop.service.AuthService;
import com.eshop.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );

        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }

        // 检查账号是否被冻结
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被冻结，请联系管理员");
        }

        // 检查是否被锁定
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException("账号已被锁定，请稍后再试");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            int errorCount = (user.getLoginErrorCount() == null ? 0 : user.getLoginErrorCount()) + 1;
            user.setLoginErrorCount(errorCount);
            if (errorCount >= 3) {
                user.setLockUntil(LocalDateTime.now().plusMinutes(15));
            }
            userMapper.updateById(user);
            throw new BusinessException("账号或密码错误");
        }

        // 登录成功，重置错误计数
        user.setLoginErrorCount(0);
        user.setLockUntil(null);
        userMapper.updateById(user);

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .storeName(user.getStoreName())
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        // 检查手机号是否已注册
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getPhone())
        );
        if (count > 0) {
            throw new BusinessException("手机号已注册，请直接登录");
        }

        User user = new User();
        user.setUsername(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : "用户" + dto.getPhone().substring(7));
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public LoginVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return LoginVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .storeName(user.getStoreName())
                .build();
    }
}
