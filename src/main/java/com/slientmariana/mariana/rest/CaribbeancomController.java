package com.slientmariana.mariana.rest;

import com.slientmariana.mariana.service.caribeancom.CaribbeancomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

@RestController
@RequestMapping("caribbeancom")
public class CaribbeancomController {

    @Autowired
    private CaribbeancomService caribbeancomService;

    @GetMapping("/get/")
    public void getCaribbeancom(){
        caribbeancomService.CreateCaribbeancom();
    }

    @GetMapping("/test/download")
    public void testDownload(){
        try {
            URL url = new URL("https://www.caribbeancom.com/moviepages/020222-001/images/l_l.jpg");
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());

            FileOutputStream fileOutputStream = new FileOutputStream("/Users/mariana/Downloads/l_l.jpg");
            fileOutputStream.getChannel()
                    .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

            readableByteChannel.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
