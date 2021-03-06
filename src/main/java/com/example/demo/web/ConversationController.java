package com.example.demo.web;


import com.example.demo.dto.MessageDto;
import com.example.demo.dto.QuestionShortDto;
import com.example.demo.dto.ReplyShortDto;
import com.example.demo.entity.Question;
import com.example.demo.exception.RecordNotFoundException;
import com.example.demo.constants.InternalMessage;
import com.example.demo.constants.ExternalMessage;
import com.example.demo.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API controller to expose a set of endpoints to retrieve and add messages and replies. That can be used as a template to develop a forum system.
 * Available on /questions/ URL segment
 */
@Slf4j
@RestController
@RequestMapping(value = "/questions")
public class ConversationController {

    /**
     * Injected Conversation Service
     */
    @Autowired
    private ConversationService conversationService;


    /**
     * GET operation to expose all questions at http://localhost:{PORT}/questions
     * {PORT} value is from application configuration server.port: XXXX
     * Returns response code 200 on success and 500 in case of abnormal execution
     * @return List of Questions
     */
    @GetMapping()
    public ResponseEntity<List<QuestionShortDto>> getAllQuestions() {
        val methodName = "QController.getAllQuestions";
        try {
            val list = conversationService.listQuestions().stream().map(Question::ToShortDto).collect(Collectors.toList());
            log.info(String.format(InternalMessage.INFO_MSG_NO_PARAM_TMPLT, methodName, list.size()));
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error(String.format(InternalMessage.ERROR_MSG_TMPLT, methodName, e.getMessage()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET operation to expose a question by Id at http://localhost:{PORT}/questions/{questionId}
     * {PORT} value is from application configuration server.port: XXXX
     * Returns response code 200 on success, 400 if record with {questionId} is not found and 500 in case of abnormal execution
     * @param id Question Id
     * @return Question instance
     */
    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestion(@PathVariable("id") long id) {
        val methodName = "QController.getQuestion";
        try {
            val question = conversationService.findQuestion(id);
            log.info(String.format(InternalMessage.INFO_MSG_1_PARAM_TMPLT, methodName, id, 1));
            return new ResponseEntity<>(question, HttpStatus.OK);
        } catch (RecordNotFoundException e) {
            log.error(String.format(InternalMessage.ERROR_MSG_RECORD_NOT_FOUND_TMPLT, methodName, id));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExternalMessage.RECORD_NOT_FOUND);
        } catch (DataAccessException e) {
            log.error(String.format(InternalMessage.ERROR_MSG_RECORD_NOT_FOUND_TMPLT, methodName, id));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * POST operation to add a new Question at at http://localhost:{PORT}/questions
     * {PORT} value is from application configuration server.port: XXXX
     * @param message Question content
     * @return newly added Question instance (short version with no replies)
     */
    @PostMapping()
    public ResponseEntity<QuestionShortDto> createQuestion(@RequestBody MessageDto message) {
        val methodName = "QController.createQuestion";
        try {
            val newQuestion = conversationService.createQuestion(message);
            log.info(String.format(InternalMessage.INFO_MSG_CREATED_SUCCESS_TMPLT, methodName, message.toString(), newQuestion.getId()));
            return new ResponseEntity<>(newQuestion.ToShortDto(), HttpStatus.CREATED);
        } catch (DataAccessException e) {
            log.error(String.format(InternalMessage.ERROR_MSG_CREATED_FAILED_TMPLT, methodName, message.toString(), e.getMessage()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExternalMessage.FAILED_TO_CREATE_RECORD);
        }
    }

    /**
     * POST operation to add a new Reply to a Question at http://localhost:{PORT}/questions/{questionId}/reply
     * {PORT} value is from application configuration server.port: XXXX
     * @param message Reply content
     * @param questionId Question the Reply is added to
     * @return newly added Reply instance
     */
    @PostMapping("/{id}/reply")
    public ResponseEntity<ReplyShortDto> createReplyForQuestion(@PathVariable("id") long questionId, @RequestBody MessageDto message) {
        val methodName = "QController.createReplyForQuestion";
        Question question;
        try {
            question = conversationService.findQuestion(questionId);
        } catch (RecordNotFoundException e) {
            log.error(String.format(InternalMessage.ERROR_MSG_RECORD_NOT_FOUND_TMPLT, methodName, questionId));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ExternalMessage.RECORD_NOT_FOUND);
        } catch (DataAccessException e) {
            log.error(String.format(InternalMessage.ERROR_MSG_RECORD_NOT_FOUND_TMPLT, methodName, questionId));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        log.info(String.format(InternalMessage.INFO_MSG_FOUND_PARENT_RECORD, methodName, questionId));

        try {
            val newReply = conversationService.createReplyForQuestion(question, message);
            log.info(String.format(InternalMessage.INFO_MSG_CREATED_SUCCESS_TMPLT, methodName, message.toString(), newReply.getId()));
            return new ResponseEntity<>(newReply.ToShortDto(), HttpStatus.CREATED);
        } catch (DataAccessException e) {
            log.error(String.format(InternalMessage.ERROR_MSG_CREATED_FAILED_TMPLT, methodName, message.toString(), e.getMessage()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ExternalMessage.FAILED_TO_CREATE_RECORD);
        }
    }

}
