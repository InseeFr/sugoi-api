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
import fr.insee.sugoi.core.exceptions.ApplicationAlreadyExistException;
import fr.insee.sugoi.core.exceptions.ApplicationNotCreatedException;
import fr.insee.sugoi.core.exceptions.ApplicationNotFoundException;
import fr.insee.sugoi.core.exceptions.GroupAlreadyExistException;
import fr.insee.sugoi.core.exceptions.GroupNotCreatedException;
import fr.insee.sugoi.core.exceptions.GroupNotFoundException;
import fr.insee.sugoi.core.exceptions.InvalidUserStorageException;
import fr.insee.sugoi.core.exceptions.OrganizationAlreadyExistException;
import fr.insee.sugoi.core.exceptions.OrganizationNotCreatedException;
import fr.insee.sugoi.core.exceptions.OrganizationNotFoundException;
import fr.insee.sugoi.core.exceptions.RealmAlreadyExistException;
import fr.insee.sugoi.core.exceptions.RealmNotCreatedException;
import fr.insee.sugoi.core.exceptions.RealmNotFoundException;
import fr.insee.sugoi.core.exceptions.UserAlreadyExistException;
import fr.insee.sugoi.core.exceptions.UserNotCreatedException;
import fr.insee.sugoi.core.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class SugoiAdviceController {

  @ExceptionHandler(RealmNotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(RealmNotFoundException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
    return response;
  }

  @ExceptionHandler(UserNotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(UserNotFoundException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
    return response;
  }

  @ExceptionHandler(OrganizationNotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(OrganizationNotFoundException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
    return response;
  }

  @ExceptionHandler(ApplicationNotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(ApplicationNotFoundException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
    return response;
  }

  @ExceptionHandler(GroupNotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(GroupNotFoundException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
    return response;
  }

  @ExceptionHandler(RealmAlreadyExistException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(RealmAlreadyExistException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.CONFLICT);
    return response;
  }

  @ExceptionHandler(ApplicationAlreadyExistException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(ApplicationAlreadyExistException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.CONFLICT);
    return response;
  }

  @ExceptionHandler(OrganizationAlreadyExistException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(OrganizationAlreadyExistException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.CONFLICT);
    return response;
  }

  @ExceptionHandler(UserAlreadyExistException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(UserAlreadyExistException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.CONFLICT);
    return response;
  }

  @ExceptionHandler(GroupAlreadyExistException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(GroupAlreadyExistException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.CONFLICT);
    return response;
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(AccessDeniedException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.FORBIDDEN);
    return response;
  }

  @ExceptionHandler(InvalidUserStorageException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(InvalidUserStorageException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
    return response;
  }

  @ExceptionHandler(RealmNotCreatedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(RealmNotCreatedException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
    return response;
  }

  @ExceptionHandler(ApplicationNotCreatedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(ApplicationNotCreatedException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
    return response;
  }

  @ExceptionHandler(OrganizationNotCreatedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(OrganizationNotCreatedException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
    return response;
  }

  @ExceptionHandler(UserNotCreatedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(UserNotCreatedException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
    return response;
  }

  @ExceptionHandler(GroupNotCreatedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(GroupNotCreatedException e) {
    ErrorView errorView = new ErrorView();
    errorView.setMessage(e.getMessage());
    final ResponseEntity<ErrorView> response =
        new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
    return response;
  }
}
