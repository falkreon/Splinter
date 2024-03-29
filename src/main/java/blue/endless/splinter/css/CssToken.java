package blue.endless.splinter.css;

public record CssToken(CssTokenType tokenType, String value) {
	public static final CssToken CDO       = new CssToken(CssTokenType.CDO, "<!--");
	public static final CssToken CDC       = new CssToken(CssTokenType.CDC, "-->");
	public static final CssToken COLON     = new CssToken(CssTokenType.COLON, ":");
	public static final CssToken SEMICOLON = new CssToken(CssTokenType.SEMICOLON, ";");
	public static final CssToken COMMA     = new CssToken(CssTokenType.COMMA, ",");
	public static final CssToken LBRACKET = new CssToken(CssTokenType.LBRACKET, "[");
	public static final CssToken RBRACKET = new CssToken(CssTokenType.RBRACKET, "]");
	public static final CssToken LPAREN = new CssToken(CssTokenType.LPAREN, "(");
	public static final CssToken RPAREN = new CssToken(CssTokenType.RPAREN, ")");
	public static final CssToken LBRACE = new CssToken(CssTokenType.LBRACE, "{");
	public static final CssToken RBRACE = new CssToken(CssTokenType.RBRACE, "}");
	public static final CssToken EOF = new CssToken(CssTokenType.EOF, "");
	
	public CssToken(CssTokenType tokenType) {
		this(tokenType, "");
	}
	
	public CssToken(CssTokenType tokenType, char representation) {
		this(tokenType, ""+representation);
	}
	
	public static CssToken delim(int codePoint) {
		return new CssToken(CssTokenType.DELIMITER, "" + (char) codePoint);
	}
	
	public static CssToken lbrace() {
		return new CssToken(CssTokenType.LBRACE, "[");
	}
	
	@Override
	public String toString() {
		String cleanValue = value.replace("\n", "\\n").replace("\t", "\\t");
		
		return switch(tokenType) {
		case DELIMITER -> "{DELIM: '" + cleanValue + "'}";
		case WHITESPACE -> "{WHITESPACE: \"" + cleanValue + "\"}";
		case CDO -> "{CDO}";
		case CDC -> "{CDC}";
		case COLON -> "{COLON}";
		case SEMICOLON -> "{SEMICOLON}";
		case COMMA -> "{COMMA}";
		case LBRACKET -> "{LBRACKET}";
		case RBRACKET -> "{RBRACKET}";
		case LPAREN -> "{LPAREN}";
		case RPAREN -> "{RPAREN}";
		case LBRACE -> "{LBRACE}";
		case RBRACE -> "{RBRACE}";
		case EOF -> "{EOF}";
		default -> "{" + tokenType + ": " + cleanValue + "}";
		};
	}
}
