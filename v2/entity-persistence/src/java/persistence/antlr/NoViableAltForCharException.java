package persistence.antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.jGuru.com
 * Software rights: http://www.antlr.org/license.html
 *
 */

public class NoViableAltForCharException extends RecognitionException {
    public char foundChar;

    public NoViableAltForCharException(char c, CharScanner scanner) {
        super("NoViableAlt", scanner.getFilename(),
              scanner.getLine(), scanner.getColumn());
        foundChar = c;
    }

    /** @deprecated As of ANTLR 2.7.2 use {@see #NoViableAltForCharException(char, String, int, int) } */
    public NoViableAltForCharException(char c, String fileName, int line) {
        this(c, fileName, line, -1);
    }
    
    public NoViableAltForCharException(char c, String fileName, int line, int column) {
        super("NoViableAlt", fileName, line, column);
        foundChar = c;
    }

    /**
     * Returns a clean error message (no line number/column information)
     */
    public String getMessage() {
        String mesg = "unexpected char: ";

        // I'm trying to mirror a change in the C++ stuff.
        // But java seems to lack something isprint-ish..
        // so we do it manually. This is probably to restrictive.

        if ((foundChar >= ' ') && (foundChar <= '~')) {
            mesg += '\'';
            mesg += foundChar;
            mesg += '\'';
        }
        else {
            mesg += "0x";

            int t = (int)foundChar >> 4;

            if (t < 10)
                mesg += (char)(t | 0x30);
            else
                mesg += (char)(t + 0x37);

            t = (int)foundChar & 0xF;

            if (t < 10)
                mesg += (char)(t | 0x30);
            else
                mesg += (char)(t + 0x37);
        }
        return mesg;
    }
}
