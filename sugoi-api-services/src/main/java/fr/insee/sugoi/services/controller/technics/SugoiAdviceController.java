package fr.insee.sugoi.services.controller.technics;

import fr.insee.sugoi.services.view.ErrorView;
import fr.insee.sugoi.sugoiapicore.utils.Exceptions.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.insee.sugoi.sugoiapicore.utils.Exceptions.RealmNotFoundException;

@ControllerAdvice
public class SugoiAdviceController {

    @ExceptionHandler(RealmNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ErrorView> exception(RealmNotFoundException e) {
        ErrorView errorView = new ErrorView();
        errorView.setMessage(e.getMessage());
        final ResponseEntity<ErrorView> response = new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
        return response;

    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ErrorView> exception(EntityNotFoundException e) {
        ErrorView errorView = new ErrorView();
        errorView.setMessage(e.getMessage());
        final ResponseEntity<ErrorView> response = new ResponseEntity<ErrorView>(errorView, HttpStatus.NOT_FOUND);
        return response;
    }

}
