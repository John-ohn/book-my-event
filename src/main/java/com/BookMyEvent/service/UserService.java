package com.BookMyEvent.service;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

   List<UserResponseDto> findAllUserProfiles();

   UserResponseDto findUserInfoById(String id);

   UserResponseDto findUserInfoByEmail(String userEmail);

   User findUserById(String userId);


   UserResponseDto save(User user);

   UserResponseDto updateUserFields(String userId, UserUpdateDto userUpdateDto);
//   UserResponseDto updateUserFields(String userId, UserUpdateDto userUpdateDto);

   UserResponseDto updateUserAvatar(String userId,  MultipartFile userAvatar);

   String delete(String userId);

   String deleteFromAdmin(String userId);

   String banned(String email);

   String unbanned(String email);

}
