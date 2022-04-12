/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.commons.services.controller.technics;

import fr.insee.sugoi.commons.services.view.ErrorView;
import fr.insee.sugoi.core.exceptions.AppCannotManagedAttributeException;
import fr.insee.sugoi.core.exceptions.AppManagedAttributeException;
import fr.insee.sugoi.core.exceptions.ApplicationAlreadyExistException;
import fr.insee.sugoi.core.exceptions.ApplicationNotCreatedException;
import fr.insee.sugoi.core.exceptions.ApplicationNotFoundException;
import fr.insee.sugoi.core.exceptions.GroupAlreadyExistException;
import fr.insee.sugoi.core.exceptions.GroupNotCreatedException;
import fr.insee.sugoi.core.exceptions.GroupNotFoundException;
import fr.insee.sugoi.core.exceptions.IdNotMatchingException;
import fr.insee.sugoi.core.exceptions.InvalidPasswordException;
import fr.insee.sugoi.core.exceptions.InvalidTransactionIdException;
import fr.insee.sugoi.core.exceptions.InvalidUserStorageException;
import fr.insee.sugoi.core.exceptions.ManagerGroupNotFoundException;
import fr.insee.sugoi.core.exceptions.NoCertificateOnUserException;
import fr.insee.sugoi.core.exceptions.NoDomaineMappingException;
import fr.insee.sugoi.core.exceptions.OrganizationAlreadyExistException;
import fr.insee.sugoi.core.exceptions.OrganizationNotCreatedException;
import fr.insee.sugoi.core.exceptions.OrganizationNotFoundException;
import fr.insee.sugoi.core.exceptions.PasswordPolicyNotMetException;
import fr.insee.sugoi.core.exceptions.RealmAlreadyExistException;
import fr.insee.sugoi.core.exceptions.RealmNotCreatedException;
import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import fr.insee.sugoi.core.exceptions.StoragePolicyNotMetException;
import fr.insee.sugoi.core.exceptions.UserAlreadyExistException;
import fr.insee.sugoi.core.exceptions.UserNotCreatedException;
import fr.insee.sugoi.core.exceptions.UserNotFoundByMailException;
import fr.insee.sugoi.core.exceptions.UserNotFoundException;
import fr.insee.sugoi.core.exceptions.UserStorageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class SugoiAdviceController {

  private static final Logger logger = LoggerFactory.getLogger(SugoiAdviceController.class);

  @ExceptionHandler(RealmNotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(RealmNotFoundException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserNotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(UserNotFoundException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserStorageNotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(UserStorageNotFoundException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(NoDomaineMappingException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(NoDomaineMappingException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserNotFoundByMailException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(UserNotFoundByMailException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ManagerGroupNotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(ManagerGroupNotFoundException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(OrganizationNotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(OrganizationNotFoundException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ApplicationNotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(ApplicationNotFoundException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(GroupNotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(GroupNotFoundException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(NoCertificateOnUserException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(NoCertificateOnUserException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(RealmAlreadyExistException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(RealmAlreadyExistException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(ApplicationAlreadyExistException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(ApplicationAlreadyExistException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(OrganizationAlreadyExistException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(OrganizationAlreadyExistException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(UserAlreadyExistException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(UserAlreadyExistException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(GroupAlreadyExistException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(GroupAlreadyExistException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(AccessDeniedException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(InvalidUserStorageException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(InvalidUserStorageException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(RealmNotCreatedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(RealmNotCreatedException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ApplicationNotCreatedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(ApplicationNotCreatedException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(OrganizationNotCreatedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(OrganizationNotCreatedException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserNotCreatedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(UserNotCreatedException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(GroupNotCreatedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(GroupNotCreatedException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(StoragePolicyNotMetException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(StoragePolicyNotMetException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(InvalidPasswordException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(InvalidPasswordException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(PasswordPolicyNotMetException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(PasswordPolicyNotMetException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(UnsupportedOperationException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(UnsupportedOperationException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_IMPLEMENTED);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(HttpMessageNotReadableException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(HttpMediaTypeNotSupportedException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(HttpMediaTypeNotAcceptableException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(Exception e) {
    logger.error(e.getMessage(), e);
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(AppCannotManagedAttributeException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(AppCannotManagedAttributeException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(AppManagedAttributeException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(AppManagedAttributeException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(InvalidTransactionIdException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(InvalidTransactionIdException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(IdNotMatchingException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(IdNotMatchingException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.BAD_REQUEST);
  }
}
