import checkers.RubyCheckers;
import entities.State;
import entities.Token;
import entities.TokenType;
import utils.Buffer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Lexer {
    private State curState = State.START;
    private Buffer bf;
    private String value = "";

    public Lexer(String filePath) {
        try {
            bf = new Buffer( Files.newBufferedReader( Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Token getNextToken() throws IOException {
        Token t = getNextTokenImplementation();
        lastToken = t;
        value = "";
        setStart();
        return t;
    }

    private Token getNextTokenImplementation() throws IOException {
        boolean eof = false;
        Token res = new Token();

        while (!eof) {
            Character c = bf.next();
            if (curState.equals(State.START)) {
                if (idHandler(c, res) != null)
                    return res;
                if (symbolHandler(c, res) != null)
                    return res;
                if (stringLiteralHandler(c, res) != null)
                    return res;
                if (heredocHandler(c, res) != null)
                    return res;
                if (plusOrMinus(c, res) != null)
                    return res;
                if (operatorHandler(c, res) != null)
                    return res;
                if (numberHandler(c, res) != null)
                    return res;
                if (punctuationHandler(c, res) != null)
                    return res;
                if (whitespaceHandler(c,res)!=null)
                    return res;
                if (commentHandler(c, res) != null)
                    return res;

            }

            if (c == (char) 0)
                eof = true;
        }
        return null;
    }

    private Token punctuationHandler(Character c, Token t) {
        switch (c) {
            case ',':
                curState = State.COMA;
                break;
            case '\\':
                curState = State.BACKSLASH;
                break;
            case '(':
                curState = State.LBRACE;
                break;

            case ')':
                curState = State.RBRACE;
                break;
            case '{':
                curState = State.L_FIG_BRACE;
                break;
            case '}':
                curState = State.R_FIG_BRACE;
                break;
            case '[':
                curState = State.L_SQ_BRACE;
                break;
            case ']':
                curState = State.R_SQ_BRACE;
                break;
            case ':':
                curState = State.DOUBLE_DOT;
                break;
            case ';':
                curState = State.DOT_COMA;
                break;
            case '.':
                curState = State.DOT;
                break;

            default:
                value = "";
                setStart();
                return null;
        }
        t.setContent(Character.toString(c));
        t.setTokenType( TokenType.SEPARATOR );

        setStart();
        return t;
    }

    private Token symbolHandler(Character c, Token t) throws IOException {
        if (c == ':') {
            curState = State.SYMBOL_START;
            value = Character.toString(c);
        } else {
            setStart();
            return null;
        }
        Character k = bf.next();
        while (k != (char) 0) {
            switch (curState) {
                case SYMBOL_START:
                    if (Character.toString(k).matches("[_0-9a-zA-Z]")) {
                        value += Character.toString(k);
                        curState = State.SYMBOL_START;
                        k = bf.next();
                    } else {
                        finalizeWithBufferBack(t, Character.toString(k), TokenType.SYMBOL);
                        curState = State.SYMBOL;
                    }
                    break;
                case SYMBOL:
                    return t;
                default:
                    return null;
            }
        }
        return null;
    }

    private Token idHandler(Character c, Token t) throws IOException {
        Character k = c;
        if (Character.toString(k).matches("[$@_a-zA-Z]")) {
            curState = State.IDENTIFIER_START;
        } else {
            setStart();
            value = "";
            return null;
        }
        value = Character.toString(k);
        k = bf.next();

        while (k != (char) 0) {
            switch (curState) {
                case IDENTIFIER_START:
                    if (Character.toString(k).matches("[@_a-zA-Z0-9]")) {
                        value += Character.toString(k);
                        curState = State.IDENTIFIER;
                        k = bf.next();
                    } else {
                        curState = State.IDENTIFIER_FOUND_ESCAPE;
                    }
                    break;
                case IDENTIFIER:
                    if (Character.toString(k).matches("[_a-zA-Z0-9]")) {
                        value += Character.toString(k);
                        curState = State.IDENTIFIER;
                        k = bf.next();
                    } else if (RubyCheckers.isSeparator(k) || RubyCheckers.isOperatorStart(k)|| RubyCheckers.isSpacing(k)) {
                        curState = State.IDENTIFIER_FOUND_ESCAPE;
                    } else {
                        curState = State.IDENTIFIER_TRAIL_ERROR;
                        value += Character.toString(k);
                        k = bf.next();
                    }
                    break;
                case IDENTIFIER_FOUND_ESCAPE:
                    if (RubyCheckers.isKeyword(value))
                        finalizeWithBufferBack(t, k, TokenType.KEYWORD);
                    else if (RubyCheckers.isLiteral(value))
                        finalizeWithBufferBack(t, k, TokenType.STRING);
                    else
                        finalizeWithBufferBack(t, k, TokenType.IDENTIFIER);
                    return t;

                case IDENTIFIER_TRAIL_ERROR:
                    if (RubyCheckers.isSeparator(k)) {
                        finalizeWithBufferBack(t, k, TokenType.ERROR);
                        return t;
                    } else {
                        value += Character.toString(k);
                        k = bf.next();
                    }
                    break;
                default:
                    return null;
            }
        }
        return null;
    }


    private Token plusOrMinus(Character c, Token t) throws IOException {
        if (c == '+' || c == '-') {
            if (lastToken != null) {
                if (lastToken.getTokenType() == TokenType.SEPARATOR ||
                        lastToken.getTokenType() == TokenType.OPERATOR) {
                    value = Character.toString(c);

                    Character k = bf.next();

                    if (!isDigit(k)) {
                        finalizeWithTokenType(t, TokenType.OPERATOR, Character.toString(c));
                        bf.back(Character.toString(k));
                        return t;
                    }

                    Token tryNum = numberHandler(k, t);
                    if (tryNum == null) {
                        value = Character.toString(c);
                        finalizeWithBufferBack(t, Character.toString(k), TokenType.OPERATOR);
                        return t;
                    } else
                        return tryNum;
                } else {
                    Character k = bf.next();
                    if (k == '=') {
                        finalizeWithTokenType(t, TokenType.OPERATOR, Character.toString(c) + k);
                        return t;
                    } else {
                        value = Character.toString(c);
                        finalizeWithBufferBack(t, Character.toString(k), TokenType.OPERATOR);
                        return t;
                    }
                }
            }
        }

        return null;
    }



    private Token numberHandler(Character c, Token t) throws IOException {
        if (!isDigit(c)) {
            return null;
        } else {
            value += Character.toString(c);
            curState = State.NUMBER_START;
        }
        Character k = bf.next();
        while (k != (char) 0) {
            switch (curState) {
                case NUMBER_START:
                    if (isDigit(k)) {
                        curState = State.NUMBER_START;
                        value += Character.toString(k);
                        k = bf.next();
                    } else if (k == '.') {
                        value += Character.toString(k);
                        curState = State.NUMBER_DOUBLE;
                        k = bf.next();
                    } else if (k == 'e' || k == 'E') {
                        value += Character.toString(k);
                        curState = State.NUMBER_EXP_START;
                        k = bf.next();
                    } else if (Character.toString(k).matches("[_A-Za-z]")) {
                        curState = State.NUMBER_ERROR;
                    } else {
                        curState = State.NUMBER_ESCAPE;
                    }
                    break;

                case NUMBER_DOUBLE:
                    if (isDigit(k)) {
                        value += Character.toString(k);
                        k = bf.next();
                    } else if (k == 'e' || k == 'E') {
                        value += Character.toString(k);
                        curState = State.NUMBER_EXP_START;
                        k = bf.next();
                    } else if (k == '.') {
                        value = value.substring(0, value.length() - 1);
                        finalizeWithBufferBack(t, "..", TokenType.NUMBER);
                        curState = State.NUMBER_RETURN;
                        k = bf.next();
                    } else {
                        curState = State.NUMBER_ESCAPE;
                    }
                    break;
                case NUMBER_EXP_START:
                    if (k == '-' || k == '+') {
                        curState = State.NUMBER_EXP_SIGNED;
                        value += Character.toString(k);
                        k = bf.next();
                    } else if (isDigit(k)) {
                        curState = State.NUMBER_EXP_UNSIGNED;
                        value += Character.toString(k);
                        k = bf.next();
                    } else {
                        curState = State.NUMBER_ERROR;
                    }
                    break;

                case NUMBER_EXP_UNSIGNED:
                case NUMBER_EXP_SIGNED:
                    if (isDigit(k)) {
                        value += Character.toString(k);
                        k = bf.next();
                    } else if (Character.toString(k).matches("[_A-Za-z]")) {
                        curState = State.NUMBER_ERROR;
                    } else {
                        finalizeWithTokenType(t, TokenType.NUMBER, value);
                        curState = State.NUMBER_RETURN;
                    }
                    break;

                case NUMBER_ERROR:
                    if (RubyCheckers.isSeparator(k)) {
                        bf.back(Character.toString(k));
                        finalizeWithTokenType(t, TokenType.ERROR, value);
                        return t;
                    } else {
                        value += Character.toString(k);
                        k = bf.next();
                    }
                    break;


                case NUMBER_ESCAPE:
                    finalizeWithBufferBack(t, k, TokenType.NUMBER);
                    return t;
                case NUMBER_RETURN:
                    return t;
                default:
                    return null;
            }
        }
        return null;
    }

    private void finalizeWithBufferBack(Token t, Character k, TokenType number) {
        t.setTokenType(number);
        t.setContent(value);
        bf.back(Character.toString(k));
        setStart();
        value = "";
    }

    private void finalizeWithBufferBack(Token t, String k, TokenType number) {
        t.setTokenType(number);
        t.setContent(value);
        bf.back(k);
        setStart();
        value = "";
    }

    private void finalizeWithTokenType(Token t, TokenType number, String value) {
        t.setTokenType(number);
        t.setContent(value);
        setStart();
    }

    private Token operatorHandler(Character c, Token t) throws IOException {
        value = "";
        if (RubyCheckers.isPartOfOperations(Character.toString(c))) {
            curState = State.OPERATION;
            value += Character.toString(c);
        } else {
            return null;
        }
        Character k = bf.next();
        while (curState.equals(State.OPERATION)) {
            if (RubyCheckers.isPartOfOperations(value + Character.toString(k))) {
                value += Character.toString(k);
                k = bf.next();

            } else {
                break;
            }
        }
        finalizeWithTokenType(t, TokenType.OPERATOR, value);
        bf.back(Character.toString(k));
        return t;
    }

    private Token whitespaceHandler(Character c, Token t) throws IOException {
        if (RubyCheckers.isSpacing(c)) {
            if (c == '\n') {
                finalizeWithTokenType(t, TokenType.WHITESPACE, "\\n");
                return t;
            }
            finalizeWithTokenType(t, TokenType.WHITESPACE, Character.toString(c));
            return t;
        } else {
            return null;
        }

    }

    private Token stringLiteralHandler(Character c, Token t) throws IOException {
        value = "";
        if (c == '\'') {
            strLiteralSearch(c, t, '\'');
            return t;
        } else if (c == '\"') {
            strLiteralSearch(c, t, '\"');
            return t;
        }
        return null;
    }

    private void strLiteralSearch(Character c, Token t, char c2) throws IOException {
        if (c == c2) {
            curState = State.STR_LIT;
            value = Character.toString(c);
        }
        Character k = bf.next();
        while (k != (char) 0) {
            switch (curState) {
                case STR_LIT:
                    if (k == c2) {
                        curState = State.STR_LIT_FINAL;
                        value += wrap(k);
                    } else if (k == '\n') {
                        curState = State.STR_LIT_ERROR;
                    } else {
                        curState = State.STR_LIT;
                        value += wrap(k);
                        k = bf.next();
                    }
                    break;
                case STR_LIT_FINAL:
                    finalizeWithTokenType(t, TokenType.STRING, value);
                    return;
                case STR_LIT_ERROR:
                    bf.back(Character.toString(k));
                    finalizeWithTokenType(t, TokenType.ERROR, value);
                    return;
                default:
                    t = null;
                    return;
            }
        }
    }

    private Token commentHandler(Character c, Token t) throws IOException {
        if (c == '#') {
            value = Character.toString(c);
            curState = State.COMMENT_SINGLE_LINE;
        }
        Character k = bf.next();
        while (k != (char) 0) {
            switch (curState) {
                case COMMENT_SINGLE_LINE:
                    if (k != '\n') {
                        value += wrap(k);
                        k = bf.next();
                        curState = State.COMMENT_SINGLE_LINE;
                    } else {
                        curState = State.COMMENT_SINGLE_LINE_END;
                    }
                    break;
                case COMMENT_SINGLE_LINE_END:
                    finalizeWithBufferBack(t, k, TokenType.COMMENT);
                    return t;
                default:
                    return null;
            }
        }

        return null;
    }

    private Token heredocHandler(Character c, Token t) throws IOException {
        value = "";
        Character k = c;
        if (k == '<') {
            curState = State.HEREDOC_FIRST;
            value += Character.toString(k);
            k = bf.next();
        } else return null;
        if (k == '<') {
            curState = State.HEREDOC_SECOND;
            value += Character.toString(k);
        } else {
            bf.back(Character.toString(k));
            setStart();
            value = "";
            return null;
        }
        k = bf.next();
        if (!Character.toString(k).matches("[_a-zA-Z]")) {
            value += wrap(k);
            bf.back(value.substring(1));
            setStart();
            value = "";
            return null;
        } else {
            curState = State.HEREDOC_START;
            value += wrap(k);
        }
        while (!value.matches("<<[_a-zA-Z][_a-zA-Z0-9]*[.,\n\r\t\\s]")) {
            k = bf.next();
            value += wrap(k);
        }
        String memorized = value.substring(2, value.length() - 1);
        curState = State.HEREDOC_FOUND_ID;

        while (!value.endsWith("\n" + memorized) && k != (char) 0) {
            k = bf.next();
            value += wrap(k);
        }
        finalizeWithTokenType(t, TokenType.STRING, value);

        return t;
    }

    private static String wrap(Character c) {
        String app = Character.toString(c);
        if (c == '\\')
            app = "\\";
        return app;
    }

    private static boolean isDigit(Character k) {
        return Character.toString(k).matches("[0-9]");
    }

    private void setStart() {
        curState = State.START;
    }


    private Token lastToken = null;
}