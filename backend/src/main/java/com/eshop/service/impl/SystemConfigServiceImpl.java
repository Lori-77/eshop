package com.eshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eshop.common.BusinessException;
import com.eshop.entity.SystemConfig;
import com.eshop.mapper.SystemConfigMapper;
import com.eshop.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    @Override
    public List<SystemConfig> getAllConfigs() {
        return systemConfigMapper.selectList(null);
    }

    @Override
    public SystemConfig getByKey(String key) {
        return systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key)
        );
    }

    @Override
    public void updateConfig(Long id, String value) {
        SystemConfig config = systemConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("配置项不存在");
        }
        config.setConfigValue(value);
        systemConfigMapper.updateById(config);
    }

    @Override
    public void addConfig(SystemConfig config) {
        systemConfigMapper.insert(config);
    }

    @Override
    public void deleteConfig(Long id) {
        systemConfigMapper.deleteById(id);
    }
}
