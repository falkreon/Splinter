package blue.endless.splinter.css;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

public class CssTokenizer {
	private static final char[] HEX_DIGITS = new char[] {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'a', 'b', 'c', 'd', 'e', 'f',
		'A', 'B', 'C', 'D', 'E', 'F'
	};
	
	static {
		Arrays.sort(HEX_DIGITS);
	}
	
	private final LookaheadReader in;
	
	public CssTokenizer(Reader r) {
		in = new LookaheadReader(r);
	}
	
	public CssTokenizer(String s) {
		this(new StringReader(s));
	}
	
	/**
	 * Returns true if the code point is whitespace according to w3c rules.
	 * @param i a code point
	 * @return true if it's whitespace, otherwise false
	 */
	private boolean isWhitespace(int i) {
		return
				i == ' ' ||
				i == '\t' ||
				i == '\n';
	}
	
	/**
	 * Returns true if the code point is a hex digit according to w3c rules.
	 * @param i a code point
	 * @return true if it's a hex digit, otherwise false
	 */
	private boolean isHexDigit(int i) {
		return Arrays.binarySearch(HEX_DIGITS, (char) i) >= 0; //Could also be done more slowly with Pattern
	}
	
	/**
	 * Returns true if the two code points form a valid escape sequence according to w3c rules.
	 * @param i the "current input code point"
	 * @param j the "next input code point"
	 * @return true if this is a valid escape sequence, otherwise false
	 */
	private boolean isValidEscape(int i, int j) {
		return i == '\\' && j != '\n';
	}
	
	/**
	 * Returns true if the peeked String of two characters is a valid escape sequence according to w3c rules.
	 * @param s a String consisting of the next two code points in the stream.
	 * @return true if the String describes a valid escape sequence, otherwise false
	 */
	private boolean isValidEscape(String s) {
		return s.length() == 2 && isValidEscape(s.charAt(0), s.charAt(1));
	}
	
	/**
	 * Consume an escaped code point. It assumes that the U+005C REVERSE SOLIDUS (\) has already been consumed and that
	 * the next input code point has already been verified to be part of a valid escape. It will return a code point.
	 * @return the code point encoded by this escape sequence.
	 */
	private int consumeEscape() throws IOException {
		int ch = in.read();
		if (ch == -1) return 0xFFFD; //This is a parse error. Return U+FFFD REPLACEMENT CHARACTER
		if (isHexDigit(ch)) {
			//Consume as many hex digits as possible, but no more than 5. Note that this means 1-6 hex digits have been consumed in total.
			StringBuilder hex = new StringBuilder();
			hex.append((char) ch);
			for(int i=0; i<5; i++) {
				if (!isHexDigit(in.peek())) break;
			}
			//If the next input code point is whitespace, consume it as well.
			if (isWhitespace(in.peek())) in.read();
			
			//Interpret the hex digits as a hexadecimal number.
			try {
				int value = Integer.parseInt(hex.toString(), 16);
				
				//If this number is zero, or is for a surrogate, or is greater than the maximum allowed code point, return U+FFFD REPLACEMENT CHARACTER
				if (value == 0 || value > 0x10FFFF || (value < 0xFFFF && Character.isSurrogate((char) value))) return 0xFFFD;
				
				//Otherwise, return the code point with that value.
				return value;
			} catch (Throwable t) {
				return 0xFFFD; //Emit replacement char on parse error per w3c guidelines.
			}
			
		} else {
			//Return the input code point
			return ch;
		}
	}
	
	private boolean isIdentStartCodePoint(int i) {
		return
				(i>='a' && i<='z') ||
				(i>='A' && i<='Z') ||
				(i == '_') ||
				(i > 0xFF); //non-ascii
	}
	
	private boolean isIdentCodePoint(int i) {
		return
				(i>='a' && i<='z') ||
				(i>='A' && i<='Z') ||
				(i>='0' && i<='9') ||
				(i == '_') ||
				(i == '-') ||
				(i > 0xFF); //non-ascii
	}
	
	/**
	 * Returns true if the peeked three code points start a valid identifier sequence according to w3c rules.
	 * @return true if the characters start an identifier, otherwise false
	 */
	private boolean isIdentStart(int i, int j, int k) {
		if (isIdentStartCodePoint(i)) return true;
		
		if (i == '-') {
			if (j == '-' || isIdentStartCodePoint(j) || isValidEscape(j, k)) return true;
		} else if (isValidEscape(i, j)) return true;
		
		return false;
	}
	
	private boolean isIdentStart(String s) {
		if (s.length() != 3) return false;
		int i = s.charAt(0);
		int j = s.charAt(1);
		int k = s.charAt(2);
		return isIdentStart(i, j, k);
	}
	
	/**
	 * Consumes an ident sequence from a stream of code points. Returns a string containing the largest name that can be
	 * formed from adjacent code points in the stream, starting from the first.
	 * 
	 * <p>Note: This algorithm does not do the verification of the first few code points that are necessary to ensure
	 * the returned code points would constitute an IdentToken. If that is the intended use, ensure that the stream
	 * starts with an ident sequence before calling this algorithm.
	 * 
	 * @return a String of the consumed Ident sequence.
	 * @throws IOException if there was a problem reading character data
	 */
	private String readIdentSequence() throws IOException {
		StringBuilder result = new StringBuilder();
		
		//Repeatedly consume the next input code point from the stream:
		int ch = in.read();
		while(!in.eof()) {
			if (isIdentCodePoint(ch)) {
				//ident code point: Append the code point to result.
				result.append((char) ch);
			} else if (ch == '\\') {
				String maybeEscape = ((char) ch) + in.peek(2);
				if (isValidEscape(maybeEscape)) {
					//the stream starts with a valid escape: Consume an escaped code point. Append the returned code point to result.
					result.append(consumeEscape());
				} else {
					break;
				}
			} else {
				//Reconsume the current input code point. Return result.
				in.pushback(ch);
				break;
			}
			
			ch = in.read();
		}
		
		return result.toString();
	}
	
	private CssToken consumeUrlToken() throws IOException {
		return new CssToken(CssTokenType.BAD_URL, "https://www.example.com/");
	}
	
	private CssToken consumeIdentLikeToken() throws IOException {
		String identSequence = readIdentSequence();
		
		//If string’s value is an ASCII case-insensitive match for "url", and the next input code point is U+0028 LEFT PARENTHESIS (()
		if (identSequence.equalsIgnoreCase("url") && in.peek() == '(') {
			//consume it.
			in.read();
			
			//While the next two input code points are whitespace, consume the next input code point.
			while(in.peek(2).isBlank()) in.read();
			
			//If the next one or two input code points are ("), ('), or whitespace followed by (") ('),
			//then create a <function-token> with its value set to string and return it.
			//Otherwise, consume a url token, and return it.
			/*
			 * basically, what they're saying here is that a url token is something like `url( https://example.com/ )`
			 * but a function token is something like the `url(` part of `url( "https://example.com/" )`. The quotation
			 * marks change our understanding of the token's identity.
			 */
			
			String next = in.peek(2);
			if (next.length() == 2) {
				int i = next.charAt(0);
				int j = next.charAt(1);
				if (
						(i == '\'' || i == '"') || //if the first character is a quote, or
						(isWhitespace(i) && (j == '\'' || j == '"'))) { //the first character is whitespace and the second character is a quote
					
					//Don't read the arguments, just return the function name as a FunctionToken representing the function identifier and the open-parentheses.
					return new CssToken(CssTokenType.FUNCTION, identSequence);
				} else {
					//Those things *aren't* true, the only remaining possibility is an unquoted url, so consume a UrlToken and return it.
					return consumeUrlToken();
				}
			} else {
				return consumeUrlToken();
			}
		} else {
			//Otherwise, if the next input code point is U+0028 LEFT PARENTHESIS (()
			if (in.peek() == '(') {
				//consume it.
				in.read();
				
				//Create a FunctionToken with its value set to string and return it.
				return new CssToken(CssTokenType.FUNCTION, identSequence);
			}
		}
		
		//Otherwise, create an IdentToken with its value set to string and return it.
		return new CssToken(CssTokenType.IDENT, identSequence);
	}
	
	/**
	 * Reads in as much whitespace as can be consumed. Returns it as a WhitespaceToken.
	 * @return a WhitespaceToken containing the whitespace removed and consumed
	 * @throws IOException if there was a problem reading character data
	 */
	private CssToken consumeWhitespaceToken() throws IOException {
		StringBuilder result = new StringBuilder();
		result.append((char) in.read());
		
		while(isWhitespace(in.peek())) {
			result.append((char) in.read());
		}
		
		return new CssToken(CssTokenType.WHITESPACE, result.toString());
	}
	
	private boolean isNumberStart(int i, int j, int k) throws IOException {
		if (i == '+' || i == '-') {
			if (Character.isDigit(j)) return true;
			
			return (j == '.' && Character.isDigit(k));
		} else if (i == '.') {
			return Character.isDigit(j);
		} else {
			return Character.isDigit(i);
		}
	}
	
	private String consumeNumber() throws IOException {
		StringBuilder result = new StringBuilder();
		
		//If the next input code point is U+002B PLUS SIGN (+) or U+002D HYPHEN-MINUS (-), consume it and append it to repr.
		int leadingChar = in.peek();
		if (leadingChar == '+' || leadingChar == '-') result.append((char) in.read());
		
		//While the next input code point is a digit, consume it and append it to repr.
		while(Character.isDigit(in.peek())) {
			result.append((char) in.read());
		}
		
		//If the next 2 input code points are U+002E FULL STOP (.) followed by a digit, then:
		String maybeFloat = in.peek(2);
		if (maybeFloat.length() == 2 && maybeFloat.startsWith(".") && Character.isDigit(maybeFloat.charAt(1))) {
			//Consume them, Append them to repr, and Set type to "number".
			result.append(in.read(2));
			
			//While the next input code point is a digit, consume it and append it to repr.
			while(Character.isDigit(in.peek())) {
				result.append((char) in.read());
			}
		}
		
		//If the next 2 or 3 input code points are (E) or (e), optionally followed by (-) or (+), followed by a digit, then:
		/*
		 * let's break this down.
		 * - if the next 3 input points are [Ee], [-+], Digit:
		 * OR
		 * - if the next 2 input points are [Ee], Digit:
		 * 
		 * --Falk
		 */
		String maybeExponent = in.peek(3);
		int a = (maybeExponent.length()>=1) ? maybeExponent.charAt(0) : -1;
		int b = (maybeExponent.length()>=2) ? maybeExponent.charAt(1) : -1;
		int c = (maybeExponent.length()>=3) ? maybeExponent.charAt(2) : -1;
		
		if (a=='e' || a=='E') {
			if (b=='-' || b=='+') {
				if (Character.isDigit(c)) {
					//Yes (3-character). Consume them, append them to repr, set the type to "number"
					result.append(in.read(3));
					
					//While the next input code point is a digit, consume it and append it to repr.
					while(Character.isDigit(in.peek())) {
						result.append((char) in.read());
					}
				}
			} else if (Character.isDigit(b)) {
				//Yes (2-character). Consume them, append them to repr, set the type to "number"
				result.append(in.read(2));
				
				//While the next input code point is a digit, consume it and append it to repr.
				while(Character.isDigit(in.peek())) {
					result.append((char) in.read());
				}
			}
		}
		
		return result.toString();
	}
	
	private CssToken consumeNumberToken() throws IOException {
		//Consume a number and let number be the result.
		String number = consumeNumber();
		
		//If the next 3 input code points would start an ident sequence, then:
		String maybeIdent = in.peek(3);
		if (isIdentStart(maybeIdent)) {
			//Create a <dimension-token> with the same value and type flag as number, and a unit set initially to the empty string. 
			//Consume an ident sequence. Set the <dimension-token>’s unit to the returned value. 
			String ident = readIdentSequence();
			return new CssToken(CssTokenType.DIMENSION, number + " " + ident); //TODO: preserve split values instead of de-tokenizing them like this
		} else if (in.peek() == '%') {
			//Otherwise, if the next input code point is (%), consume it. Create a <percentage-token> with the same value as number, and return it.
			in.read();
			return new CssToken(CssTokenType.PERCENTAGE, number);
		}
		
		return new CssToken(CssTokenType.NUMBER, number);
	}
	
	//W3C specifies that the function may be called this way, and what happens when it is called. But we don't use it here.
	/*
	private CssToken consumeStringToken() throws IOException {
		int endingToken = in.read();
		return consumeStringToken(endingToken);
	}*/
	
	private CssToken consumeStringToken(int endingToken) throws IOException {
		StringBuilder result = new StringBuilder();
		while(!in.eof()) {
			int cur = in.read();
			if (cur == endingToken) {
				return new CssToken(CssTokenType.STRING, result.toString());
			} else if (cur == -1) {
				//This is a parse error. Return the StringToken.
				return new CssToken(CssTokenType.STRING, result.toString());
			} else if (cur == '\n') {
				in.pushback(cur);
				return new CssToken(CssTokenType.BAD_STRING, result.toString());
			} else if (cur == '\\') {
				int nextCodePoint = in.peek();
				//If the next input code point is EOF, do nothing.
				if (nextCodePoint == -1) continue; //this will naturally wrap around to the EOF case above, log the error, and return STRING
				
				//Otherwise, if the next input code point is a newline, consume it.
				if (nextCodePoint == '\n') {
					in.read();
				} else {
					//We don't need to test if the stream starts with a valid escape, we have already satisfied these conditions.
					result.appendCodePoint(consumeEscape());
				}
			} else {
				result.append((char) cur);
			}
		}
		
		//should never reach here. Same as EOF case above: log a parse error and return the string
		return new CssToken(CssTokenType.STRING, result.toString());
	}
	
	public CssToken next() throws IOException {
		int codePoint = in.read();
		
		if (isWhitespace(codePoint)) {
			//Consume as much whitespace as possible and return WhitespaceToken
			in.pushback(codePoint);
			return consumeWhitespaceToken();
		}
		
		if (Character.isDigit(codePoint)) {
			//pushback the input code point
			in.pushback(codePoint);
			//consume a numeric token, and return it. 
			return consumeNumberToken();
		}
		
		if (isIdentStartCodePoint(codePoint)) {
			//Reconsume the current input code point, consume an ident-like token, and return it.
			in.pushback(codePoint);
			return consumeIdentLikeToken();
		}
		
		return switch(codePoint) {
			case '\"' -> {
				yield consumeStringToken(codePoint);
			}
			case '#' -> {
				/**
				 * If the next input code point is an ident code point or the next two input code points are a valid escape, then:
				 * - Create a HashToken
				 * - If the next 3 input code points would start an ident sequence, set the HashToken’s type flag to "id".
				 * - Consume an ident sequence, and set the HashToken’s value to the returned string.
				 * - return the HashToken
				 * 
				 * If that input code point was NOT an ident code point or we weren't starting an escape, return the octothorpe as a DelimToken
				 */
				if (isIdentCodePoint(in.peek()) || isValidEscape(in.peek(2))) {
					String identStart = in.peek(3);
					if (isIdentStart(identStart)) {
						//I'm choosing to interpret this as, not to consume an ident sequence if it's not an ident sequence.
						//This is consistent with the railroad diagrams.
						yield new CssToken(CssTokenType.HASH, readIdentSequence());
					}
				}
				yield CssToken.delim('#');
			}
			case '\'' -> {
				yield consumeStringToken(codePoint);
			}
			
			case '+' -> {
				int j = in.peek();
				int k = in.peekFurther(1);
				if (isNumberStart(codePoint, j, k)) {
					in.pushback(codePoint);
					yield consumeNumberToken();
				}
				//If the three code points were not a numberStart, return the + as DelimToken
				yield CssToken.delim(codePoint);
			}
			
			case '-' -> {
				int j = in.peek();
				int k = in.peekFurther(1);
				if (isNumberStart(codePoint, j, k)) {
					in.pushback(codePoint);
					yield consumeNumberToken();
				}
				
				if (j == '-' && k == '>') {
					//consume the additional code points
					in.read(); in.read();
					yield CssToken.CDC;
				}
				
				//Otherwise, if the input stream starts with an ident sequence
				if (isIdentStart(codePoint, j, k)) {
					//reconsume the current input code point, consume an ident-like token, and return it.
					in.pushback(codePoint);
					yield consumeIdentLikeToken();
				}
				
				//Otherwise, return the + as DelimToken
				yield CssToken.delim(codePoint);
			}
			case '.' -> {
				//If the input stream starts with a number
				int j = in.peek();
				int k = in.peekFurther(1);
				if (isNumberStart(codePoint, j, k)) {
					//reconsume the current input code point, consume a numeric token, and return it.
					in.pushback(codePoint);
					yield consumeNumberToken();
				}
				//Otherwise return the period as a DelimToken.
				yield CssToken.delim('.');
			}
			case '<' -> {
				/*
				 * If the next 3 input code points are "!--" (as in the full sequence was "<!--",
				 * Otherwise return the less-than as a DelimToken
				 */
				String jkl = in.peek(3);
				if (jkl.equals("!--")) {
					//consume them and return a CDOToken.
					in.read(3);
					yield CssToken.CDO;
				}
				yield CssToken.delim(codePoint);
			}
			case '@' -> {
				//If the next 3 input code points would start an ident sequence,
				int j = in.peek();
				int k = in.peekFurther(1);
				int l = in.peekFurther(2);
				if (isIdentStart(j,k,l)) {
					//consume an ident sequence, create an AtKeywordToken with its value set to the ident sequence, and return it.
					String keyword = readIdentSequence();
					yield new CssToken(CssTokenType.AT_KEYWORD, keyword);
				}
				//Otherwise return the @ as a DelimToken
				yield CssToken.delim(codePoint);
			}
			case '\\' -> {
				//If the next code point is a valid escape,
				if (isValidEscape(codePoint, in.peek())) {
					//push the backslash back, read and return an IdentLikeToken.
					in.pushback(codePoint);
					yield consumeIdentLikeToken();
				}
				//If this isn't a valid escape, log a parse error here and return the slash as a DelimToken
				yield CssToken.delim(codePoint);
			}
			case '(' -> CssToken.LPAREN;
			case ')' -> CssToken.RPAREN;
			case ',' -> CssToken.COMMA;
			case ':' -> CssToken.COLON;
			case ';' -> CssToken.SEMICOLON;
			case '[' -> CssToken.LBRACKET;
			case ']' -> CssToken.RBRACKET;
			case '{' -> CssToken.LBRACE;
			case '}' -> CssToken.RBRACE;
			case -1  -> CssToken.EOF;
			default  -> CssToken.delim(codePoint);
		};
	}
}
