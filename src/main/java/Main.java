import entities.Token;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        printTokensFromLexer("./src/main/resources/input.rb");
    }

    private static void printTokensFromLexer(String inputPath) throws IOException {
        Lexer lexer = new Lexer(inputPath);
        FileWriter outputWriter = new FileWriter("output.txt");
        Token t = lexer.getNextToken();
        while (t != null) {
            outputWriter.write( String.valueOf( t ) );
            outputWriter.write('\n');
            t = lexer.getNextToken();
        }
        outputWriter.close();
    }
}
