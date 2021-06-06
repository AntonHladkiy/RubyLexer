package entities;

import java.util.*;

public class Keywords {
    public static final List<String> keywords = Arrays.asList(
            "BEGIN", "END", "__ENCODING__", "__END__", "__FILE__", "__LINE__",
            "alias", "and",
            "begin", "break",
            "case", "class",
            "def", "defined?", "do",
            "else", "elsif", "end", "ensure",
            "for",
            "if", "in", "include",
            "module", "next", "nil", "not",
            "or",
            "redo", "rescue", "retry", "return",
            "self", "super",
            "then",
            "undef", "unless", "until",
            "when", "while",
            "yield"
    );

    public static final List<String> operations = Arrays.asList(
            "==", "!=", ">", "<", ">=", "<=", "<=>", "===", ".eql?", "equal?",
            "+=", "-=", "*=", "/=", "%=", "**=",
            "<<", ">>", "&&", "||", "!",
            "&", "|", "^", "~",
            "+", "-", "*", "/", "%", "**",
            "=",
            "..", "...",
            "defined?"
    );


    public static final List<String> literals = Arrays.asList(
            "true", "false", "null", "TRUE", "FALSE", "NIL"
    );

}
