package com.slientmariana.mariana.config.resttemplate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Component
@Slf4j
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse)
        throws IOException{
        return (
                httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                || httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse)
        throws IOException{
        if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR){
            if (httpResponse.getStatusCode() == HttpStatus.NOT_FOUND){
                log.error("Status Code: {}, Message: {}", httpResponse.getStatusCode(), httpResponse.getStatusText());
            }
        }
    }
}
