package blue.endless.splinter.css;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A map of css property keys to values
 */
public class Style {
	private static final List<CssToken> EMPTY_PROPERTY = List.of();
	
	protected Map<String, List<CssToken>> data = new HashMap<>();
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
	public List<CssToken> getRaw(PropertyKey<?> key) {
		return data.getOrDefault(key.propertyName(), EMPTY_PROPERTY);
	}
	
	/**
	 * Gets the raw token-list mapped to this property
	 * @param propertyName the name for the property to retrieve
	 * @return The list of tokens used to specify this property, or an empty list if no data is specified.
	 */
	public List<CssToken> getRaw(String propertyName) {
		return data.getOrDefault(propertyName, EMPTY_PROPERTY);
	}
	
	public boolean has(PropertyKey<?> key) {
		return data.containsKey(key.propertyName());
	}
	
	public Style put(PropertyKey<?> key, List<CssToken> rawValue) {
		data.put(key.propertyName(), rawValue);
		return this;
	}
}
