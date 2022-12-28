package com.slientmariana.mariana.tools;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.slientmariana.mariana.vo.MovieNfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service("tools")
@Slf4j
public class ToolsImpl implements Tools{

    @Value("${app.movie.directory.temporary}")
    private String temporaryDirectory;

    @Value("${app.movie.directory.mariana}")
    private String marianaDirectory;

    @Override
    public void DownloadFile(URL url, String destination){

        ReadableByteChannel readableByteChannel = null;
        FileOutputStream fileOutputStream = null;
        try {

            readableByteChannel = Channels.newChannel(url.openStream());
            fileOutputStream = new FileOutputStream(destination);
            fileOutputStream.getChannel()
                    .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null){
                    fileOutputStream.close();
                }
                if (readableByteChannel != null){
                    readableByteChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void CreateNFOFile(MovieNfo nfo){
        String movieDirectory = ConcatenateMovieDirectory(nfo);
        Path movieDirectoryPath = Paths.get(movieDirectory.trim());
        String nfoFileName = String.format("%s.nfo", nfo.getTitle().trim());
        XmlMapper xmlMapper = new XmlMapper();

        try {
            xmlMapper.writeValue(new File(String.valueOf(movieDirectoryPath), nfoFileName), nfo);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void CopyDirectoryToServer(String studioEnglishName, MovieNfo nfo) throws IOException {
        String sourceDirectory = ConcatenateMovieDirectory(nfo);
        String destinationDirectory = String.format("%s/Studio/%s/%s/%s", marianaDirectory,studioEnglishName,nfo.getYear(),nfo.getTitle().trim());
        FileUtils.copyDirectory(new File(sourceDirectory), new File(destinationDirectory));
    }

    private String ConcatenateMovieDirectory(MovieNfo nfo){
        return String.format("%s/%s",temporaryDirectory, nfo.getTitle().trim());
    }


}
