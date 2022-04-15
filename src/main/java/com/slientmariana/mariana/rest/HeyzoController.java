package com.slientmariana.mariana.rest;

import com.slientmariana.mariana.service.heyzo.HeyzoService;
import com.slientmariana.mariana.vo.MovieNfo;
import com.slientmariana.mariana.vo.MovieRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("heyzo")
public class HeyzoController {

    @Autowired
    private HeyzoService heyzoService;

    @PostMapping("/")
    public MovieNfo getHeyzo(@RequestBody MovieRequestDTO dto){
        return heyzoService.CreateHeyzo(dto);
    }

}
