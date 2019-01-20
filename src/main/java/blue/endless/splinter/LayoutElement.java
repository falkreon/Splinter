package blue.endless.splinter;

public interface LayoutElement {
	default int getNaturalWidth() { return 0; }
	default int getNaturalHeight() { return 0; }
}
