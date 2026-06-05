package com.eshop.service;

import com.eshop.dto.AddressDTO;
import com.eshop.entity.Address;
import com.eshop.vo.UserVO;

import java.util.List;

public interface UserService {
    UserVO getProfile(Long userId);
    void updateProfile(Long userId, UserVO vo);

    // 地址管理
    List<Address> getAddresses(Long userId);
    Address getAddressById(Long id);
    void saveAddress(Long userId, AddressDTO dto);
    void updateAddress(Long userId, AddressDTO dto);
    void deleteAddress(Long userId, Long addressId);
}
