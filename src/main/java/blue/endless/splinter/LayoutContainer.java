package blue.endless.splinter;

import java.util.Collection;

public interface LayoutContainer {
	public Iterable<? extends LayoutElement> getLayoutChildren();
	public LayoutElementMetrics getLayoutElementMetrics(LayoutElement elem);
	
	/**
	 * Gets the layout-related settings for this container
	 */
	public LayoutContainerMetrics getLayoutContainerMetrics();
	
	/** Notifies the host container of grid metrics, in case it can draw visible gridlines or boxes.
	 * 
	 * @param rowSizes The calculated height of each grid row. Grid row 0 occupies from Y coordinate 0 to rowHeights[0]-1,
	 *                 while grid row 1 occupes rowHeights[0] to rowHeights[0]+rowHeights[1]-1. In other words,
	 *                 grid borders fall on the line between two canvas pixels!
	 * @param columnSizes The calculated width of each grid column.
	 */
	default void setGridMetrics(Collection<Integer> rowPositions, Collection<Integer> rowSizes, Collection<Integer> columnPositions, Collection<Integer> columnSizes) {}
	
	/**
	 * Sets layout values on this component to ones decided by the layout system.
	 * @param x The component's new x value
	 * @param y The component's new y value
	 * @param width The component's new width
	 * @param height The component's new height
	 */
	void setLayoutValues(LayoutElement elem, int x, int y, int width, int height);
}
