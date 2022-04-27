package com.kazurayam.materialstore.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

public final class StringUtils {

    private StringUtils() {}

    public static String join(List<String> lines) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        for (String line : lines) {
            pw.println(line);
        }
        pw.flush();
        pw.close();
        return sw.toString();
    }

    public static String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static String indentLines(String text) {
        return indentLines(text, 4);
    }

    public static String indentLines(String text, int indentWidth) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        StringReader sr = new StringReader(text);
        BufferedReader br = new BufferedReader(sr);
        String line;
        try {
            while ((line = br.readLine()) != null) {
                pw.println(String.join("", Collections.nCopies(indentWidth, " ")) + line);
            }
            br.close();
            sr.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pw.flush();
        pw.close();
        return sw.toString();
    }
}
