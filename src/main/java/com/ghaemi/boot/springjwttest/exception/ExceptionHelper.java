package com.ghaemi.boot.springjwttest.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class ExceptionHelper {
    public  ResponseEntity<Map<String, Object>> genError(int status, String error,Object message){
        var map = new HashMap<String,Object>();
        map.put("timestamp", Instant.now().toString());
        map.put("status", status);
        map.put("error", error);
        map.put("message", message);
        return ResponseEntity.status(status).body(map);
    }
}
