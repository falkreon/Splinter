package blue.endless.splinter.css;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import blue.endless.splinter.data.Size;
import blue.endless.splinter.data.SizeUnit;

public class PropertyKeys {
	//public static final PropertyKey<Integer> ACCENT_COLOR = new PropertyKey<>("accent_color", null); //TODO: Add deserializer
	
	public static final PropertyKey<Size> WIDTH = new PropertyKey<>("width", PropertyKeys::sizeDeserializer);
	public static final PropertyKey<Size> HEIGHT = new PropertyKey<>("height", PropertyKeys::sizeDeserializer);
	
	
	public static Optional<Size> sizeDeserializer(List<CssComponent> components) {
		if (components.isEmpty()) return Optional.empty();
		CssComponent comp = components.get(0);
		if (comp.tokenType() == CssTokenType.DIMENSION) {
			String[] pieces = comp.value().split(" ");
			if (pieces.length==2) {
				try {
					double d = Double.parseDouble(pieces[0]);
					String unit = pieces[1].trim().toLowerCase(Locale.ROOT);
					SizeUnit actualUnit = switch(unit) {
						case "px" -> SizeUnit.PIXELS;
						case "pt" -> SizeUnit.POINTS;
						case "em" -> SizeUnit.EMS;
						default -> SizeUnit.PIXELS;
					};
					return Optional.of(new Size((int) d, actualUnit));
				} catch (NumberFormatException ex) {}
			}
		} else if (comp.tokenType() == CssTokenType.PERCENTAGE) {
			try {
				double d = Double.parseDouble(comp.value());
				return Optional.of(new Size((int) d, SizeUnit.PERCENT));
			} catch (NumberFormatException ex) {}
		}
		
		return Optional.empty();
	}
}
