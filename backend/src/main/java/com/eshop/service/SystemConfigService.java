package com.eshop.service;

import com.eshop.entity.SystemConfig;
import java.util.List;

public interface SystemConfigService {
    List<SystemConfig> getAllConfigs();
    SystemConfig getByKey(String key);
    void updateConfig(Long id, String value);
    void addConfig(SystemConfig config);
    void deleteConfig(Long id);
}
