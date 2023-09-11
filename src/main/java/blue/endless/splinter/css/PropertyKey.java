package blue.endless.splinter.css;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final record PropertyKey<T>(String propertyName, Function<List<CssComponent>, Optional<T>> deserializer) {
}
