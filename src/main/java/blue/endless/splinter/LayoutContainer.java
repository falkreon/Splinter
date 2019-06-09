/*
 * MIT License
 *
 * Copyright (c) 2019 Falkreon (Isaac Ellingson)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
	default void setGridMetrics(GridMetrics metrics) {}
	
	/**
	 * Sets layout values on this component to ones decided by the layout system.
	 * @param x The component's new x value
	 * @param y The component's new y value
	 * @param width The component's new width
	 * @param height The component's new height
	 */
	void setLayoutValues(LayoutElement elem, int x, int y, int width, int height);
}
