package com.hanyahunya.task.application.exception;

/**
 * Task 생성/수정 시 Trigger 또는 Action의 config가
 * ModuleCapability의 paramSchema와 일치하지 않을 때 발생하는 예외
 */
public class InvalidConfigException extends RuntimeException {
    public InvalidConfigException(String message) {
        super(message);
    }
}
