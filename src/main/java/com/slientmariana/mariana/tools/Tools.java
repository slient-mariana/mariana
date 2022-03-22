package com.slientmariana.mariana.tools;

import com.slientmariana.mariana.vo.MovieNfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;

public interface Tools {

    void DownloadFile(URL url, String destination);
    void CreateNFOFile(MovieNfo nfo);
    void CopyDirectoryToServer(String studioEnglishName ,MovieNfo nfo) throws IOException;
}
