package blue.endless.splinter.css;

public class CssParseError extends Exception {
	private static final long serialVersionUID = 1L;

	public CssParseError() {
	}
	
	public CssParseError(String message) {
		super(message);
	}
	
	public CssParseError(String message, Throwable cause) {
		super(message, cause);
	}
}
