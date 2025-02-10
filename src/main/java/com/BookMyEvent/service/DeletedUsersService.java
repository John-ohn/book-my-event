package com.BookMyEvent.service;

public interface DeletedUsersService {

  void addUserToDeletedList(String userEmail);

  boolean emailExist(String userEmail);
}
