package entities;

public class Token {

    private String content;
    private TokenType tokenType;

    public void setContent(String content) {
        this.content = content;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }


    public Token() {
    }

    public TokenType getTokenType() {
        return tokenType;
    }


    private static String addSpaces(String s, int n) {
        StringBuilder res = new StringBuilder(s);
        while (res.length() < n) {
            res.append(" ");
        }
        return res.toString();
    }



    @Override
    public String toString() {
        return "( " + addSpaces(tokenType.toString(), 5) + "  :  " + content + " )";
    }
}