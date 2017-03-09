package com.pipeline;

import java.nio.charset.Charset;
import java.util.List;

public interface Filepipeline {

    void printResult(String resource, boolean append);

    List<String> readLines(Charset charset);
}
