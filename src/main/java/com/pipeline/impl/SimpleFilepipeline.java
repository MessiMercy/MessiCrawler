package com.pipeline.impl;

import com.google.common.io.Files;
import com.pipeline.Filepipeline;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by Administrator on 2016/11/30.
 */
public class SimpleFilepipeline implements Filepipeline {
    private File file;
    private final Logger logger = Logger.getLogger(this.getClass());

    static {
        PropertyConfigurator.configure("log4j.properties");
    }

    private SimpleFilepipeline() {
    }

    public SimpleFilepipeline(File file) {
        this.file = file;
        if (!file.exists()) {
            try {
                File parentFile = file.getCanonicalFile().getParentFile();
                if (!parentFile.exists()) parentFile.mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void printResult(String resource, boolean append) {
        if (StringUtils.isEmpty(resource)) return;
        try (FileWriter writer = new FileWriter(file, append)) {
            writer.write(resource);
            writer.flush();
        } catch (IOException e1) {
            logger.error(e1.toString());
        }
    }

    @Override
    public List<String> readLines(Charset charset) {
        try {
            return Files.readLines(this.file, charset);
        } catch (IOException e) {
            logger.error(e.toString());
        }
        return null;
    }

    private void setFile(File file) {
        this.file = file;
    }
}
