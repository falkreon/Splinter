package blue.endless.splinter.css;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class CssParser {
	private final LinkedList<CssComponent> components = new LinkedList<>();
	
	private CssParser() {}
	
	public CssParser(List<CssToken> tokens) {
		normalizeTokenStream(new LinkedList<>(tokens));
	}
	
	public CssParser(String s) throws IOException {
		LinkedList<CssToken> tokens = new LinkedList<>();
		CssTokenizer tokenizer = new CssTokenizer(s);
		CssToken cur = tokenizer.next();
		while(cur.tokenType() != CssTokenType.EOF) {
			tokens.add(cur);
			cur = tokenizer.next();
		}
		normalizeTokenStream(tokens);
	}
	
	/**
	 * Parses from a list of already-normalized components. This is used by some W3C algorithms to parse temporary sublists.
	 * @param components A list of CssComponents that are already normalized (folded up so children are inside their parents instead of to the right of them).
	 * @return A CssParser that sees the component list as the entire document
	 */
	public static CssParser ofComponents(List<CssComponent> components) {
		CssParser result = new CssParser();
		result.components.addAll(components);
		return result;
	}
	
	/**
	 * Note: This algorithm assumes that the current input token has already been checked to be a FunctionToken.
	 */
	private CssComponent consumeFunction(LinkedList<CssToken> tokens, CssToken startToken) {
		List<CssComponent> block = new ArrayList<>();
		
		CssToken cur = (tokens.isEmpty()) ? CssToken.EOF : tokens.removeFirst();
		while (cur.tokenType() != CssTokenType.EOF) {
			if (cur.tokenType() == CssTokenType.RPAREN) break; //return the function
			
			//Push it back and return it as a component
			tokens.addFirst(cur);
			block.add(consumeComponentValue(tokens));
			
			if (tokens.isEmpty()) break;
			cur = tokens.removeFirst();
		}
		
		return new CssComponent(CssTokenType.FUNCTION, startToken.value(), block);
	}
	
	private CssComponent consumeSimpleBlock(LinkedList<CssToken> tokens, CssToken startToken) {
		CssTokenType endType = switch(startToken.tokenType()) {
			case LBRACE -> CssTokenType.RBRACE;
			case LBRACKET -> CssTokenType.RBRACKET;
			case LPAREN -> CssTokenType.RPAREN;
			case FUNCTION -> CssTokenType.RPAREN;
			default -> throw new IllegalArgumentException();
		};
		
		List<CssComponent> block = new ArrayList<>();
		
		CssToken cur = tokens.removeFirst();
		while (cur.tokenType() != CssTokenType.EOF) {
			if (cur.tokenType() == endType) break;
			
			tokens.addFirst(cur);
			block.add(consumeComponentValue(tokens));
			
			if (tokens.isEmpty()) break;
			cur = tokens.removeFirst();
			//If cur's type is EOF, log an error
		}
		
		return new CssComponent(startToken.tokenType(), startToken.value(), block);
	}
	
	private CssComponent consumeComponentValue(LinkedList<CssToken> tokens) {
		CssToken token = tokens.removeFirst();
		if (token.tokenType() == CssTokenType.LPAREN) {
			return consumeSimpleBlock(tokens, token);
		} else if (token.tokenType() == CssTokenType.LBRACE) {
			return consumeSimpleBlock(tokens, token);
		} else if (token.tokenType() == CssTokenType.LBRACKET) {
			return consumeSimpleBlock(tokens, token);
		} else if (token.tokenType() == CssTokenType.FUNCTION) {
			return consumeFunction(tokens, token);
		} else {
			return new CssComponent(token);
		}
	}
	
	/**
	 * Discards any whitespace components at the front of the component buffer.
	 */
	public void discardLeadingWhitespace() {
		while (!components.isEmpty() && components.peekFirst().tokenType() == CssTokenType.WHITESPACE) components.removeFirst();
	}
	
	private CssComponent nextComponent() {
		if (components.isEmpty()) return new CssComponent(CssTokenType.EOF, "");
		return components.removeFirst();
	}
	
	private CssComponent peek() {
		if (components.isEmpty()) return new CssComponent(CssTokenType.EOF, "");
		return components.peekFirst();
	}
	
	private void pushback(CssComponent component) {
		components.addFirst(component);
	}
	
	public CssComponent consumeAtRule() {
		CssComponent result = nextComponent();
		
		//Shouldn't happen: fail as quickly and completely as possible if it does so it can get fixed
		if (result.tokenType() != CssTokenType.AT_KEYWORD) throw new IllegalStateException("Tried to build an at-rule out of the wrong token!");
		
		while (!components.isEmpty()) {
			CssComponent cur = nextComponent();
			
			switch (cur.tokenType()) {
				case SEMICOLON -> { return result; }
				case EOF -> { break; } //This is a parse error.
				//Technically there is a case for {, but these have already been componentized and will be added by the case below.
				default -> result.children().add(cur);
			}
		}
		
		return result;
	}
	
	public Optional<CssComponent> consumeQualifiedRule() {
		StringBuilder prelude = new StringBuilder();
		
		while (!components.isEmpty()) {
			CssComponent cur = nextComponent();
			
			switch(cur.tokenType()) {
				case EOF -> { return Optional.empty(); } //This is a parse error.
				case LBRACE -> {
					return Optional.of(new CssComponent(CssTokenType.IDENT, prelude.toString(), cur.children()));
				}
				default -> {
					if (prelude.length() != 0) prelude.append(' ');
					prelude.append(cur.representation());
				}
			}
		}
		
		return Optional.empty();
	}
	
	/* 5.4.4 Consume a style block’s contents */
	
	public List<CssComponent> consumeStyleBlockContents() {
		List<CssComponent> decls = new ArrayList<>();
		List<CssComponent> rules = new ArrayList<>();
		
		loop:
		while(!components.isEmpty()) {
			CssComponent cur = nextComponent();
			
			switch(cur.tokenType()) {
				case WHITESPACE -> {} // we don't care
				case SEMICOLON -> {}  // we also don't care about empty statements
				case EOF -> { break loop; } // This is unlikely, but a valid result condition
				
				case AT_KEYWORD -> rules.add(consumeAtRule());
				case IDENT -> {
					List<CssComponent> tempList = new ArrayList<>();
					CssComponent temp = nextComponent();
					while(temp.tokenType() != CssTokenType.SEMICOLON && temp.tokenType() != CssTokenType.EOF) {
						tempList.add(temp);
						temp = nextComponent();
					}
					
					try {
						CssComponent component = CssParser.ofComponents(tempList).readDeclaration();
						decls.add(component);
					} catch (CssParseError err) {
						//Don't add it to the list
					}
				}
				
				case DELIMITER -> {
					if (cur.value().equals("&")) {
						pushback(cur);
						consumeQualifiedRule().ifPresent(rules::add);
					} else {
						/* This is an error. Reconsume the current input token. As long as the next input token is
						 * anything other than a semicolon or EOF, consume a component value and throw away the returned
						 * value. 
						 */
						
						while(!components.isEmpty()) {
							cur = peek();
							if (cur.tokenType() == CssTokenType.SEMICOLON || cur.tokenType() == CssTokenType.EOF) break;
							nextComponent();
						}
					}
				}
				
				default -> {
					/* This is an error. Reconsume the current input token. As long as the next input token is
					 * anything other than a semicolon or EOF, consume a component value and throw away the returned
					 * value. 
					 */
					
					while(!components.isEmpty()) {
						cur = peek();
						if (cur.tokenType() == CssTokenType.SEMICOLON || cur.tokenType() == CssTokenType.EOF) break;
						nextComponent();
					}
				}
			}
		}
		
		decls.addAll(rules);
		return decls;
	}
	
	/**
	 * Reads an Ident component, which starts a style rule declaration, and folds the contents of the declaration into
	 * the ident's children.
	 * 
	 * <p><b>WARNING:</b> The W3C defines this very weirdly! It assumes the *entire component stream* is one single
	 * declaration! You probably want {@link #readDeclarationList()}
	 * @return a copy of the Ident component with the children folded in.
	 * @throws IllegalStateException if the next component is not an Ident.
	 */
	public CssComponent readDeclaration() throws CssParseError {
		if (peek().tokenType() != CssTokenType.IDENT) throw new CssParseError("Can only read a declaration when the next component is an IDENT.");
		
		CssComponent identComponent = nextComponent();
		discardLeadingWhitespace();
		if (components.isEmpty()) throw new CssParseError("Abrupt EOF while trying to get a declaration value.");
		
		//If the next input token is anything other than a <colon-token>, this is a parse error. Return nothing.
		if (peek().tokenType() != CssTokenType.COLON) throw new CssParseError("Missing colon after declaration key \""+identComponent.value()+"\".");
		nextComponent(); //consume the colon
		
		discardLeadingWhitespace();
		
		CssComponent cur = nextComponent();
		while (cur.tokenType() != CssTokenType.EOF) {
			identComponent.children().add(cur);
			cur = nextComponent();
		}
		
		return identComponent;
	}
	
	public List<CssComponent> readDeclarationList() throws CssParseError {
		List<CssComponent> declarations = new ArrayList<>();
		CssComponent cur = nextComponent();
		while(cur.tokenType() != CssTokenType.EOF) {
			switch(cur.tokenType()) {
				case WHITESPACE -> {} //Do nothing
				case SEMICOLON -> {} //Do nothing (surprising!)
				case AT_KEYWORD -> {
					pushback(cur);
					declarations.add(consumeAtRule());
				}
				case IDENT -> {
					ArrayList<CssComponent> tempList = new ArrayList<>();
					tempList.add(cur);
					
					CssComponent possibleChild = peek();
					while(possibleChild.tokenType() != CssTokenType.EOF && possibleChild.tokenType() != CssTokenType.SEMICOLON) {
						tempList.add(nextComponent());
						possibleChild = peek();
					}
					
					try {
						CssComponent component = CssParser.ofComponents(tempList).readDeclaration();
						declarations.add(component);
					} catch (CssParseError err) {
						//Don't add it to the list
					}
				}
				default -> {
					//This is a parse error.
					return declarations;
				}
			}
			
			cur = nextComponent();
		}
		
		return declarations;
	}
	
	//Destructively modifies the token list
	private void normalizeTokenStream(LinkedList<CssToken> tokens) {
		while(!tokens.isEmpty()) {
			int sizeBefore = tokens.size();
			components.add(consumeComponentValue(tokens));
			if (tokens.size() == sizeBefore) {
				//TODO: Log an error
				return;
			}
		}
	}

	public List<CssComponent> getComponents() {
		return components;
	}
}
