package checkers;

import entities.Keywords;


public class RubyCheckers {

    public static boolean isKeyword(String word) {
        return Keywords.keywords.contains(word);
    }

    public static boolean isLiteral(String word) {
        return Keywords.literals.contains(word);
    }

    public static boolean isPartOfOperations(String s) {
        for (String op : Keywords.operations) {
            if (op.startsWith(s))
                return true;
        }
        return false;
    }

    public static boolean isSpacing(Character c) {
        return Character.isWhitespace(c) || (c == '\n') ;
    }

    public static boolean isSeparator(Character c) {
        return (c == ';') || (c == ' ') || (c == '(') || (c == ')') || (c == '[') || (c == ']') || (c == '{') || (c == '}');
    }

    public static boolean isOperatorStart(Character c) {
        return (c == '+') || (c == '-') || (c == '=') || (c == '>') || (c == '<') || (c == '(') || (c == '?') ||
                (c == '!') || (c == '%') || (c == '#') || (c == ':') || (c == '.') || (c == ',');
    }
}
