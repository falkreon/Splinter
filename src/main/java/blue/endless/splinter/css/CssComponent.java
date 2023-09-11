package blue.endless.splinter.css;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The only difference between a list of tokens and a list of component values is that some objects that "contain"
 * things, like functions or blocks, are a single entity in the component-value list, but are multiple entities in a
 * token list.
 */
public record CssComponent(CssTokenType tokenType, String value, List<CssComponent> children) {
	private static final Set<CssTokenType> SIMPLE_TOKEN_TYPES = Set.of(
			CssTokenType.CDO,     CssTokenType.CDC,
			CssTokenType.COLON,   CssTokenType.SEMICOLON,
			CssTokenType.COMMA
			);
	
	private static final Set<CssTokenType> SIMPLE_BLOCK_TYPES =
			Set.of(CssTokenType.LBRACKET, CssTokenType.LPAREN, CssTokenType.LBRACE);
	
	public CssComponent(CssTokenType tokenType, String value) {
		this(tokenType, value, new ArrayList<>());
	}
	
	public CssComponent(CssToken token) {
		this(token.tokenType(), token.value(), new ArrayList<>());
	}
	
	@Override
	public String toString() {
		if (SIMPLE_TOKEN_TYPES.contains(tokenType)) return "{"+tokenType+"}";
		if (SIMPLE_BLOCK_TYPES.contains(tokenType)) return "{"+tokenType+": "+children+"}";
		
		String cleanValue = value.replace("\n", "\\n").replace("\t", "\\t");
		if (children.isEmpty()) {
			return "{"+tokenType+": "+cleanValue+"}";
		} else {
			return "{"+tokenType+": "+cleanValue+" "+children+"}";
		}
	}
	
	public String representation() {
		if (SIMPLE_TOKEN_TYPES.contains(tokenType)) {
			return value;
		}
		
		return switch(tokenType) {
			case NUMBER -> value;
			case IDENT -> value;
			case WHITESPACE -> "";
			case FUNCTION -> {
				StringBuilder result = new StringBuilder(value);
				result.append('(');
				for(CssComponent child : children) {
					result.append(child.representation());
					result.append(' ');
				}
				result.setLength(result.length() - 1);
				result.append(')');
				
				yield result.toString();
			}
			case HASH -> {
				yield '#' + value;
			}
			case STRING -> '\"' + value + '\"';
			case DIMENSION -> value;
			case PERCENTAGE -> value + "%";
			case AT_KEYWORD -> {
				if (children.isEmpty()) yield "@"+value;
				
				StringBuilder builder = new StringBuilder();
				builder.append('@');
				builder.append(value);
				builder.append("{ ");
				for(CssComponent child : children) {
					builder.append(child.representation());
					builder.append(' ');
				}
				builder.setLength(builder.length() - 1);
				builder.append("}");
				
				yield builder.toString();
			}
			default -> {
				if (children.isEmpty()) yield tokenType.name().toLowerCase(Locale.ROOT);
				
				StringBuilder builder = new StringBuilder();
				builder.append(children.get(0).representation());
				boolean first = true;
				if (children.size()>1) for (CssComponent child : children) {
					if (first) {
						first = false;
						continue;
					}
					builder.append(' ');
					builder.append(child.representation());
				}
				yield builder.toString();
			}
		};
	}
}
