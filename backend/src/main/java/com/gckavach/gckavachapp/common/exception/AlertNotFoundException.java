package com.gckavach.gckavachapp.common.exception;

public class AlertNotFoundException extends RuntimeException{

    public AlertNotFoundException(String alertId){
        super(alertId);
    }
}
