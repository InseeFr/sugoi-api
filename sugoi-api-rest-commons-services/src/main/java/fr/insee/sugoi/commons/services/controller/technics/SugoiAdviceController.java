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
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class SugoiAdviceController {

  private static final Logger logger = LoggerFactory.getLogger(SugoiAdviceController.class);

  @ExceptionHandler(Exception.class)
  @ResponseBody
  public ResponseEntity<ErrorView> exception(Exception e) {
    ErrorView errorView = new ErrorView(e.getMessage());
    HttpStatus status = computeStatusFromException(e);
    if (status.is5xxServerError()) {
      logger.error(e.getMessage(), e);
    }
    return new ResponseEntity<>(errorView, status);
  }

  private HttpStatus computeStatusFromException(Exception e) {
    if (e instanceof NotFoundException || e instanceof NoHandlerFoundException) {
      return HttpStatus.NOT_FOUND;
    } else if (e instanceof ConflictException) {
      return HttpStatus.CONFLICT;
    } else if (e instanceof BadRequestException
        || e instanceof HttpMessageNotReadableException
        || e instanceof HttpMediaTypeNotSupportedException
        || e instanceof HttpMediaTypeNotAcceptableException) {
      return HttpStatus.BAD_REQUEST;
    } else if (e instanceof ForbiddenException || e instanceof AccessDeniedException) {
      return HttpStatus.FORBIDDEN;
    } else if (e instanceof UnsupportedOperationException) {
      return HttpStatus.NOT_IMPLEMENTED;
    } else if (e instanceof HttpRequestMethodNotSupportedException) {
      return HttpStatus.METHOD_NOT_ALLOWED;
    } else {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }
}
