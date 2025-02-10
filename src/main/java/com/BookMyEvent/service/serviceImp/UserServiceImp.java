package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.Image;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.DeletedUsersService;
import com.BookMyEvent.service.MailService;
import com.BookMyEvent.service.MediaService;
import com.BookMyEvent.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImp implements UserService {

  private final UserRepository userRepository;
  private final DeletedUsersService deletedUsersService;
  private final UserMapper userMapper;
  private final MailService mailService;
  private final MediaService mediaService;

  @Value("${company.phone}")
  private String companyPhone;
  public static final String NOT_FOUND_MESSAGE_ID = "User with ID [%s] not found.";
  public static final String NOT_FOUND_MESSAGE_EMAIL = "User with Email [%s] not found.";
  private final String className = this.getClass().getSimpleName();

  @Override
  public List<UserResponseDto> findAllUserProfiles() {
//    List<UserResponseDto> userList = userRepository.findAllUserProfiles();
    List<User> userL = userRepository.findAll();
    log.info("{}::findAllUserProfiles. Return all existing users.", className);
    return userL.stream()
        .map(userMapper::toUserResponseDtoWithoutAvatarAndEvents)
        .toList();
  }

  @Override
  public UserResponseDto findUserInfoById(String userId) {
    if (userId == null || userId.isEmpty()) {
      log.warn("{}::findUserInfoById. Return error message.", className);
      throw new GeneralException("User ID cannot be null or empty", HttpStatus.BAD_REQUEST);
    }
//    Optional<UserResponseDto> user = userRepository.findUserInfoById(userId);
    Optional<User> user = userRepository.findById(new ObjectId(userId));
    if (user.isPresent()) {
      log.info("UserServiceImp::findUserInfoById. Return user by ID: {}.", userId);

      return userMapper.toUserResponseDtoWithEvents(user.get());
    } else {
      log.warn("UserServiceImp::findUserInfoById. Return error message.");
      throw new GeneralException(String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public UserResponseDto findUserInfoByEmail(String userEmail) {
    if (userEmail == null || userEmail.isEmpty()) {
      log.warn("UserServiceImp::findUserInfoByEmail. Return error message.");
      throw new GeneralException("User Email cannot be null or empty", HttpStatus.BAD_REQUEST);
    }
    Optional<UserResponseDto> user = userRepository.findUserInfoByEmail(userEmail);
    if (user.isPresent()) {
      log.info("UserServiceImp::findUserInfoByEmail. Return user by Email: {}.", userEmail);

      return user.get();
    } else {
      log.warn("UserServiceImp::findUserInfoByEmail. Return error message.");
      throw new GeneralException(String.format(NOT_FOUND_MESSAGE_EMAIL, userEmail), HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public User findUserById(String userId) {
    if (userId == null || userId.isEmpty()) {
      log.warn("{}::findUserInfoById. Return error message.", className);
      throw new GeneralException("User ID cannot be null or empty", HttpStatus.BAD_REQUEST);
    }
    User user = userRepository.findById(new ObjectId(userId)).orElseThrow(
        () -> {
          log.warn("UserServiceImp::findUserInfoById. Return error message.");
          return new GeneralException(String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
        }
    );
    log.info("UserServiceImp::findUserInfoById. Return user by ID: {}.", userId);

    return user;
  }

  @Override
  public UserResponseDto save(User user) {
    if (userRepository.existsByEmail(user.getEmail())) {
      log.warn("UserServiceImp::save. Return error message.");
      throw new GeneralException(String.format("User with email <%s> already exists", user.getEmail()), HttpStatus.BAD_REQUEST);
    }
    try {
      User newUser = userRepository.save(user);
      log.info("UserServiceImp::save. Return saved user by id: {}.", newUser.getId());

      return userMapper.toUserResponseDto(newUser);
    } catch (Exception e) {
      log.warn("UserServiceImp::save. Return error message : {}.", e.getMessage());
      throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public UserResponseDto updateUserFields(String userId, UserUpdateDto userUpdateDto) {
    if (userUpdateDto == null || (userId == null || userId.isEmpty())) {
      log.warn("UserServiceImp::updateFields. Return error message.");
      throw new GeneralException("Can't make changes fields is null or empty.", HttpStatus.BAD_REQUEST);
    }
    User user = userRepository.findById(new ObjectId(userId))
        .orElseThrow(() -> {
          log.warn("UserServiceImp::updateFields. Return error message.");
          return new GeneralException(String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
        });
    userMapper.mapUserUpdateToUser(userUpdateDto, user);
    User updateUser = userRepository.save(user);

    return userMapper.toUserResponseDto(updateUser);
  }

//  @Override
//  public UserResponseDto updateUserFields(String userId, UserUpdateDto userUpdateDto) {
//    if (userUpdateDto == null || (userId == null || userId.isEmpty())) {
//      log.warn("UserServiceImp::updateFields. Return error message.");
//      throw new GeneralException("Can't make changes fields is null or empty.", HttpStatus.BAD_REQUEST);
//    }
//    User user = userRepository.findById(new ObjectId(userId))
//        .orElseThrow(() -> {
//          log.warn("UserServiceImp::updateFields. Return error message.");
//          return new GeneralException(String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
//        });
//    userMapper.mapUserUpdateToUser(userUpdateDto, user);
//    User updateUser = userRepository.save(user);
//
//    return userMapper.toUserResponseDto(updateUser);
//  }

  @Override
  public UserResponseDto updateUserAvatar(String userId, MultipartFile userAvatar) {
    User user = userRepository.findById(new ObjectId(userId))
        .orElseThrow(() -> {
          log.warn("UserServiceImp::updateFields. Return error message.");
          return new GeneralException(String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
        });
    String imageName = user.getId().toHexString()+"/"+userAvatar.getOriginalFilename();
    Image newImage = mediaService.savedImg(userAvatar);
    newImage.setName(imageName);
    user.linkImageWithUser(newImage);
    User updateUser = userRepository.save(user);

    return userMapper.toUserResponseDto(updateUser);
  }

  @Override
  public String delete(String userId) {

    if (userId == null || userId.isEmpty()) {
      log.warn("UserServiceImp::delete. Return error message.");
      throw new GeneralException(String.format("User ID cannot be null or empty. %s ", userId), HttpStatus.BAD_REQUEST);
    }
    User user = userRepository.findById(new ObjectId(userId))
        .orElseThrow(() -> {
          log.warn("UserServiceImp::updateFields. Return error message.");
          return new GeneralException(String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
        });
    log.info("UserServiceImp::delete. Deleted user by ID: {}.", userId);
    if(user.getAvatarImage() != null) {
      mediaService.deleteAll(List.of(user.getAvatarImage()));
    }
    userRepository.delete(user);
    return "User was deleted successfully.";
  }

  @Override
  public String deleteFromAdmin(String userId) {
    if (userId == null || userId.isEmpty()) {
      log.warn("UserServiceImp::deleteFromAdmin. Return error message.");
      throw new GeneralException(String.format("User ID cannot be null or empty. %s ", userId), HttpStatus.BAD_REQUEST);
    }
    User user = userRepository.findById(new ObjectId(userId)).orElseThrow(() -> {
      log.warn("UserServiceImp::deleteFromAdmin. Return error message.");
      return new GeneralException(String.format(NOT_FOUND_MESSAGE_ID, userId), HttpStatus.NOT_FOUND);
    });
      log.info("UserServiceImp::deleteFromAdmin. Deleted user by ID: {}.", userId);

      deletedUsersService.addUserToDeletedList(user.getEmail());
    if(user.getAvatarImage() != null) {
      mediaService.deleteAll(List.of(user.getAvatarImage()));
    }
      userRepository.delete(user);
      return "User was deleted successfully.";
  }

  @Override
  public String banned(String email) {
    log.info("Attempting to ban user with email: {}", email);

    var userOptional = userRepository.findUserByEmail(email);
    if (userOptional.isPresent()) {
      var user = userOptional.get();
      log.info("User found: {} with current status: {}", user.getEmail(), user.getStatus());

      if (!user.getStatus().equals(Status.BANNED)) {
        user.setStatus(Status.BANNED);
        userRepository.save(user);
//        mailService.blockingMessage(user.getEmail());
        mailService.sendSimpleHtmlMailMessage4Line(user.getEmail(),
            "Твій акаунт тимчасово заблоковано – що робити далі?",
            "",
            "Твій акаунт заблоковано, доступ обмежено у зв’язку з недотриманням правил платформи.",
            "Якщо у тебе є питання, зателефонуй на нашу гарячу лінію.",
            "\uD83D\uDCF2 " + companyPhone,
            "");
        log.info("User status updated to 'BANNED' for user: {}", user.getEmail());
        return "User status updated to 'BANNED'";
      } else {
        log.warn("User with email: {} is already banned.", email);
        throw new GeneralException("User is already banned", HttpStatus.BAD_REQUEST);
      }
    } else {
      log.warn("No user found with email: {}", email);
      throw new GeneralException(
              String.format("User with such email: (%s) not found", email),
              HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public String unbanned(String email) {
    log.info("Attempting to activate user with email: {}", email);

    var userOptional = userRepository.findUserByEmail(email);
    if (userOptional.isPresent()) {
      var user = userOptional.get();
      log.info("User found: {} with current status: {}", user.getEmail(), user.getStatus());

      if (!user.getStatus().equals(Status.ACTIVE)) {
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
//        mailService.unblockingMessage(user.getEmail());
        mailService.sendSimpleHtmlMailMessage4Line(user.getEmail(),
            "Твій акаунт знову активний – ласкаво просимо назад!",
            "Вітаємо!",
            "Твій акаунт розблоковано, і ти знову можете користуватися всіма можливостями нашого сайту. Насолоджуйся!",
            "",
            "",
            "");
        log.info("User status successfully updated to 'ACTIVE' for user: {}", user.getEmail());

        return "User activated successfully";
      } else {
        log.warn("User with email: {} is already active.", email);
        throw new GeneralException("User is already active", HttpStatus.BAD_REQUEST);
//        return "User is already active";
      }
    } else {
      log.warn("No user found with email: {}", email);
      throw new GeneralException(String.format("User with such email: (%s) not found", email), HttpStatus.NOT_FOUND);
    }
  }


}
