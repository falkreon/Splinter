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

public class LayoutElementMetrics {
	public static final LayoutElementMetrics EMPTY_METRICS = new LayoutElementMetrics(-1,-1,0,0);
	
	/** The X coordinate, in cells, of the child element */
	public int cellX;
	/** The Y coordinate, in cells, of the child element */
	public int cellY;
	/** The number of cells wide the child element is permitted to occupy (usually 1) */
	public int cellsX;
	/** The number of cells high the child element is permitted to occupy (usually 1) */
	public int cellsY;
	
	//The amount of space there must be between the child element and the borders of its logical cell
	public int paddingTop    = 0;
	public int paddingLeft   = 0;
	public int paddingRight  = 0;
	public int paddingBottom = 0;
	
	public GrowType horizontalGrowType = GrowType.FILL;
	public GrowType verticalGrowType = GrowType.FILL;
	
	public Alignment horizontalAlignment = Alignment.CENTER;
	public Alignment verticalAlignment = Alignment.CENTER;
	
	/** A number of canvas pixels wide that the child component is not allowed to shrink below. */
	public int fixedMinX = -1;
	/** A percentage of *total parent width* that the child component is not allowed to shrink below. */
	public int relativeMinX = -1;
	
	/** A number of canvas pixels tall that the child component is not allowed to shrink below. */
	public int fixedMinY = -1;
	/** A percentage of *total parent height* that the child component is not allowed to shrink below. */
	public int relativeMinY = -1;
	
	//TODO: Any additional constraints to be communicated to the layout engine?
	
	public LayoutElementMetrics(int cellX, int cellY, int cellsX, int cellsY) {
		this.cellX = cellX;
		this.cellY = cellY;
		this.cellsX = cellsX;
		this.cellsY = cellsY;
	}
	
	public LayoutElementMetrics(int cellX, int cellY) {
		this.cellX = cellX;
		this.cellY = cellY;
		this.cellsX = 1;
		this.cellsY = 1;
	}
	
	/** Insets all sides of this component inwards from the borders of its logical cell */
	public void setPadding(int padding) {
		paddingTop = padding;
		paddingLeft = padding;
		paddingRight = padding;
		paddingBottom = padding;
	}
	
	/** Add a constraint on this element to be at least the specified percent of the container's width. */
	public void addMinPercentWidth(int percent) {
		relativeMinX = Math.max(relativeMinX, percent); //Largest minimum size wins
	}
	
	/** Add a constraint on this element to be at least the specified percent of the container's height. */
	public void setMinPercentHeight(int percent) {
		relativeMinY = Math.max(relativeMinX, percent); //Largest minimum size wins
	}
	
	public void setMinimumWidth(int amount) {
		fixedMinX = Math.max(fixedMinX, amount);
	}
	
	public void setMinimumHeight(int amount) {
		fixedMinY = Math.max(fixedMinY, amount);
	}
}
