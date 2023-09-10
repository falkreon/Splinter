package blue.endless.splinter.metrics;

import blue.endless.splinter.data.Axis;

public record LayoutElementMetrics(LayoutElementAxisMetrics horizontal, LayoutElementAxisMetrics vertical) {
	public LayoutElementMetrics(int x, int y) {
		this(
				new LayoutElementAxisMetrics(x),
				new LayoutElementAxisMetrics(y)
			);
	}
	
	public LayoutElementAxisMetrics getAxis(Axis axis) {
		return (axis == Axis.HORIZONTAL) ? horizontal : vertical;
	}
}
