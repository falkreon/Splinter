package blue.endless.splinter;

/**
 * Represents what happens to a LayoutElement's size when it's in a cell larger than itself.
 * Most like Swift "distribution", but also compare to android's "gravity" and swing's GridBagConstraints.fill
 */
public enum GrowType {
	/** The LayoutElement will grow no larger than its minimum size along this axis */
	PACK,
	/** The LayoutElement will grow to the largest size it can along this axis*/
	FILL;
}
