package hacker.service.parser;

/**
 * Created by sarath on 7/10/16.
 */
public class ParsingStrategy {

    private static final Parser staxParser = StaxParser.getInstance();
    private static final Parser saxParser = SaxParser.getInstance();

    public static Parser findParser(ParserType parserType){
        switch (parserType) {
            case P2: {
                return saxParser;
            }
            case P1: {
                return staxParser;
            }
            default: return staxParser;
        }
    }

    public static enum ParserType {
        P1, P2
    }

}
