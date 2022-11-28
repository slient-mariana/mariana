package com.slientmariana.mariana.rest;

import com.slientmariana.mariana.rest.dto.RequestDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbstractController {

    public void LogDTO(RequestDTO dto){
        dto.getCodes().stream()
                .forEach(this::LogCode);
    }

    private void LogCode(String code){
      log.info("Movie Code: {}", code);
    }
}
