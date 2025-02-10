package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.DeletedUsersRepository;
import com.BookMyEvent.entity.DeletedUsers;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.DeletedUsersService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeletedUsersServiceImp implements DeletedUsersService {

  private final DeletedUsersRepository deletedUsersRepository;
  @Override
  public void addUserToDeletedList(String userEmail) {
    DeletedUsers deletedUsers = new DeletedUsers(userEmail);
    try {
      deletedUsersRepository.save(deletedUsers);
    }catch (Exception e){
      throw  new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

  }

  @Override
  public boolean emailExist(String userEmail) {

    return deletedUsersRepository.existsByEmail(userEmail);
  }
}
