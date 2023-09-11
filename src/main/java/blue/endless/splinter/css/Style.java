package blue.endless.splinter.css;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A map of css property keys to values
 */
public class Style {
	private static final List<CssComponent> EMPTY_PROPERTY = List.of();
	
	protected Map<String, List<CssComponent>> data = new HashMap<>();
	//protected Map<PropertyKey<?>, Optional<Object>> memoMap = new HashMap<>();
	
	/**
	 * Gets the value associated with the specified property.
	 * @param <T> The type of the value that will be returned
	 * @param key A PropertyKey corresponding to the desired property
	 * @return If the property is present, its unpacked value is returned. If it is not, or an error occurs, empty is returned.
	 */
	//@SuppressWarnings("unchecked")
	public <T> Optional<T> get(PropertyKey<T> key) {
		//Premature optimization
		/*
		return (Optional<T>) memoMap.computeIfAbsent(
					key,
					it->Optional.ofNullable(data.get(it.propertyName()))
						.flatMap(it.deserializer())
				);
		*/
		
		return Optional.ofNullable(data.get(key.propertyName()))
				.flatMap(key.deserializer());
	}
	
	/**
	 * Gets the raw token-list mapped to this property
	 * @param key a PropertyKey for the property to retrieve
	 * @return The list of tokens used to specify this property, or an empty list if no data is specified.
	 */
	public List<CssComponent> getRaw(PropertyKey<?> key) {
		return data.getOrDefault(key.propertyName(), EMPTY_PROPERTY);
	}
	
	/**
	 * Gets the raw token-list mapped to this property
	 * @param propertyName the name for the property to retrieve
	 * @return The list of tokens used to specify this property, or an empty list if no data is specified.
	 */
	public List<CssComponent> getRaw(String propertyName) {
		return data.getOrDefault(propertyName, EMPTY_PROPERTY);
	}
	
	public boolean has(PropertyKey<?> key) {
		return data.containsKey(key.propertyName());
	}
	
	public Style put(PropertyKey<?> key, List<CssComponent> rawValue) {
		data.put(key.propertyName(), rawValue);
		return this;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		for(Map.Entry<String, List<CssComponent>> entry : data.entrySet()) {
			builder.append('\t');
			builder.append(entry.getKey());
			builder.append(": ");
			for(int i=0; i<entry.getValue().size(); i++) {
				CssComponent comp = entry.getValue().get(i);
				builder.append(comp.representation());
				if (i<entry.getValue().size()-1) builder.append(' ');
			}
			builder.append(';');
			builder.append('\n');
		}
		//if (data.entrySet().size()>0) builder.setLength(builder.length()-1);
		
		builder.append("}");
		return builder.toString();
	}
	
	public static Style of(String s) throws CssParseError {
		try {
			List<CssComponent> declarations = new CssParser(s).readDeclarationList();
			
			Style style = new Style();
			for(CssComponent comp : declarations) {
				style.data.put(comp.value(), comp.children());
			}
			
			return style;
		} catch (IOException e) {
			throw new CssParseError("Unknown error", e);
		}
	}
}
