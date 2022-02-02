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
import fr.insee.sugoi.model.exceptions.BadRequestException;
import fr.insee.sugoi.model.exceptions.ConflictException;
import fr.insee.sugoi.model.exceptions.ForbiddenException;
import fr.insee.sugoi.model.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class SugoiAdviceController {

  private static final Logger logger = LoggerFactory.getLogger(SugoiAdviceController.class);

  @ExceptionHandler(NotFoundException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(NotFoundException e) {
    ErrorView errorView = new ErrorView(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ConflictException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(ConflictException e) {
    ErrorView errorView = new ErrorView(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(BadRequestException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(BadRequestException e) {
    ErrorView errorView = new ErrorView(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ForbiddenException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(ForbiddenException e) {
    ErrorView errorView = new ErrorView(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(AccessDeniedException e) {
    ErrorView errorView = new ErrorView(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(UnsupportedOperationException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(UnsupportedOperationException e) {
    ErrorView errorView = new ErrorView(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.NOT_IMPLEMENTED);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(HttpMessageNotReadableException e) {
    ErrorView errorView = new ErrorView(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(HttpMediaTypeNotSupportedException e) {
    ErrorView errorView = new ErrorView(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(HttpMediaTypeNotAcceptableException e) {
    ErrorView errorView = new ErrorView(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(HttpRequestMethodNotSupportedException e) {
    ErrorView errorView = new ErrorView(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.METHOD_NOT_ALLOWED);
  }

  @ExceptionHandler(Exception.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(Exception e) {
    logger.error(e.getMessage(), e);
    ErrorView errorView = new ErrorView(e.getMessage());
    return new ResponseEntity<>(errorView, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
