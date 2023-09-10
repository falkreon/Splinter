/*
 * MIT License
 *
 * Copyright (c) 2019-2023 Falkreon (Isaac Ellingson)
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

import blue.endless.splinter.data.Axis;
import blue.endless.splinter.metrics.GridMetrics;
import blue.endless.splinter.metrics.LayoutContainerMetrics;
import blue.endless.splinter.metrics.LayoutElementAxisMetrics;
import blue.endless.splinter.metrics.OldLayoutElementMetrics;

public interface LayoutContainer {
	public Iterable<? extends LayoutElement> getLayoutChildren();
	public OldLayoutElementMetrics getOldLayoutElementMetrics(LayoutElement elem);
	public LayoutElementAxisMetrics getChildMetrics(LayoutElement elem, Axis axis);
	
	/**
	 * Gets the layout-related settings for this container
	 */
	public LayoutContainerMetrics getLayoutContainerMetrics();
	
	/** Notifies the host container of grid metrics, in case it can draw visible gridlines or boxes.
	 * 
	 * @param metrics the locations and spacings of cells that were assigned to this container
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
	
	/**
	 * Used by the layout system to notify the LayoutContainer of the inflexible minimum width mandated by its children
	 * @param value the natural width of the container
	 */
	void setNaturalWidth(int value);
	
	/**
	 * Called by the layout system to notify the LayoutContainer of the inflexible minimum height mandated by its children
	 * @param value the natural height of the container
	 */
	void setNaturalHeight(int value);
	
	/**
	 * Gets the minimum width of this container mandated by the children. Layout sets this value via setNaturalWidth
	 */
	int getNaturalWidth();
	
	/**
	 * Gets the minimum height of this container mandated by its children. Layout sets this value via setNaturalHeight
	 */
	int getNaturalHeight();
}
