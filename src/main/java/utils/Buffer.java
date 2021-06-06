package utils;

import java.io.BufferedReader;
import java.io.IOException;

public class Buffer {
    private String value = " ";
    private int ptr = 0;

    private static final int bsize = 64;

    private final BufferedReader br;

    public Buffer(BufferedReader br) {
        this.br = br;
    }

    public Character next() throws IOException {
        Character c = value.charAt(ptr);
        ptr++;

        if (ptr >= value.length()) {
            char[] cbuff = new char[bsize];
            br.read(cbuff, 0, bsize);
            value = String.valueOf(cbuff);
            ptr = 0;
        }

        return c;
    }

    public void back(String s) {
        value = value.substring(0, ptr) + s + value.substring(ptr );
    }
}