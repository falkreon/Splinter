package blue.endless.splinter.css;

public enum CssTokenType {
	IDENT,
	FUNCTION,
	AT_KEYWORD,
	HASH,
	STRING,
	/**
	 * A String token that started out okay but went bad somewhere along the way, and all we're interested in doing now
	 * is getting to a recovery point and moving on.
	 */
	BAD_STRING,
	URL,
	/**
	 * Much like {@link #BAD_STRING}, this token was given every opportunity to do well in the parser, and it just made
	 * bad life decisions. Its problems need to be sandboxed so that they don't become our problems.
	 */
	BAD_URL,
	DELIMITER,
	NUMBER,
	PERCENTAGE,
	DIMENSION,
	WHITESPACE,
	CDO,
	CDC,
	COLON,
	SEMICOLON,
	COMMA,
	LBRACKET,
	RBRACKET,
	LPAREN,
	RPAREN,
	LBRACE,
	RBRACE,
	
	EOF
	;
}
