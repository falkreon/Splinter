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

package blue.endless.splinter.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Represents arbitrarted grid sizes based on constraints
 */
public class GridMetrics {
	public LayoutContainerMetrics containerMetrics;
	public int width = 1;
	public int height = 1;
	public Element[] xMetrics = new Element[4];
	public Element[] yMetrics = new Element[4];
	public List<Constraint> xConstraints = new ArrayList<>();
	public List<Constraint> yConstraints = new ArrayList<>();
	
	public GridMetrics() {
		fillEmpties(xMetrics, Element::new);
		fillEmpties(yMetrics, Element::new);
	}
	
	/** Gets the width of the grid in cells */
	public int getWidth() { return width; }
	
	/** Gets the height of the grid in cells */
	public int getHeight() { return height; }
	
	/** Enlarges the grid if necessary to contain the specified cell coordinates */
	public void ensureSpaceFor(int x, int y) {
		width = Math.max(width, x+1);
		height = Math.max(height, y+1);
		
		xMetrics = reserve(xMetrics, x);
		yMetrics = reserve(yMetrics, y);
		
		fillEmpties(xMetrics, Element::new);
		fillEmpties(yMetrics, Element::new);
	}
	
	public int getCellWidth(int x) {
		if (!checkBounds(x,0)) return 0;
		return xMetrics[x].size;
	}
	
	public int getCellHeight(int y) {
		if (!checkBounds(0,y)) return 0;
		return yMetrics[y].size;
	}
	
	public int getCellLeft(int x) {
		if (!checkBounds(x,0)) return 0;
		return xMetrics[x].location;
	}
	
	public int getCellTop(int y) {
		if (!checkBounds(0,y)) return 0;
		return yMetrics[y].location;
	}
	
	/**
	 * Gets an array containing metrics for each row of the represented grid. The array may be larger than necessary, but is guaranteed not to be smaller than {@link #getHeight()}.
	 * DO NOT MODIFY the returned array! Only Layout is allowed to do that
	 */
	public Element[] getRows() {
		return yMetrics;
	}
	
	/**
	 * Gets an array containing metrics for each column of the represented grid. The array may be larger than necessary, but is guaranteed not to be smaller than {@link #getWidth()}.
	 * DO NOT MODIFY the returned array! Only Layout is allowed to do that
	 */
	public Element[] getColumns() {
		return xMetrics;
	}
	
	public void addContainerMetrics(LayoutContainerMetrics metrics) {
		containerMetrics = metrics;
	}
	
	/** Merges the given layoutMetrics with the existing ones */
	public void addElementMetrics(OldLayoutElementMetrics metrics) {
		if (metrics.cellX<0 || metrics.cellY<0 || metrics.cellX+(metrics.cellsX-1)>=width || metrics.cellY+(metrics.cellsY-1)>=height) return;
		
		if (metrics.cellsX>1) {
			if (metrics.relativeMinX>0 || metrics.fixedMinX>0) {
				Constraint constraint = new Constraint();
				constraint.fixedSize = metrics.fixedMinX;
				constraint.relativeSize = metrics.relativeMinX;
				constraint.index = metrics.cellX;
				constraint.span = metrics.cellsX;
				xConstraints.add(constraint);
				
				for(int i=0; i<metrics.cellsX; i++) {
					Element elem = xMetrics[metrics.cellX+i];
					elem.multiColumnApplied = true;
				}
			}
		} else {
			Element column = xMetrics[metrics.cellX];
			addElementMetrics(containerMetrics, column, metrics.cellX, metrics.fixedMinX, metrics.relativeMinX);
		}
		
		if (metrics.cellsY>1) {
			if (metrics.relativeMinY>0 || metrics.fixedMinY>0) {
				Constraint constraint = new Constraint();
				constraint.fixedSize = metrics.fixedMinY;
				constraint.relativeSize = metrics.relativeMinY;
				constraint.index = metrics.cellY;
				constraint.span = metrics.cellsY;
				yConstraints.add(constraint);
				
				for(int i=0; i<metrics.cellsY; i++) {
					Element elem = yMetrics[metrics.cellY+i];
					elem.multiColumnApplied = true;
				}
			}
		} else {
			Element row = yMetrics[metrics.cellY];
			addElementMetrics(containerMetrics, row, metrics.cellY, metrics.fixedMinY, metrics.relativeMinY);
		}
	}
	
	protected void addElementMetrics(LayoutContainerMetrics containerMetrics, Element existing, int index, int fixed, int relative) {
		int paddingLeading = containerMetrics.cellPadding; if (index>0) paddingLeading /= 2;
		int paddingTrailing = containerMetrics.cellPadding; if (index<width-1) paddingTrailing /= 2;
		if (fixed>0) {
			existing.fixedSize = Math.max(existing.fixedSize, fixed+paddingLeading+paddingTrailing);
		}
		existing.relativeSize = Math.max(existing.relativeSize, relative);
	}
	
	public void recalcStarts() {
		int xPos = 0;
		for(Element metrics : xMetrics) {
			metrics.location = xPos;
			xPos += metrics.size;
		}
		
		int yPos = 0;
		for(Element metrics : yMetrics) {
			metrics.location = yPos;
			yPos += metrics.size;
		}
	}
	
	private static <T> T[] reserve(T[] original, int index) {
		if (index<original.length) return original;
		int scaled = Math.max(index+1, original.length*2);
		
		T[] replacement = Arrays.copyOf(original, scaled);
		
		return replacement;
	}
	
	/**
	 * !important - each Element in the array needs to be a *unique* object because it's mutated frequently during the process of layout.
	 */
	private static <T> void fillEmpties(T[] subject, Supplier<T> supplier) {
		for(int i=0; i<subject.length; i++) {
			if (subject[i]==null) subject[i] = supplier.get();
		}
	}
	
	private boolean checkBounds(int x, int y) {
		return x>=0 && y>=0 && x<width && y<height;
	}
	
	@Override
	public String toString() {
		return "{ width: "+width+", height: "+height+", xMetrics: "+Arrays.toString(xMetrics)+", yMetrics: "+Arrays.toString(yMetrics)+" }";
	}
	
	/** Represents metrics for one row or column */
	public static class Element {
		public int fixedSize = 0;
		public int relativeSize = 0;
		public boolean multiColumnApplied = false;
		
		public int location = 0;
		public int size = 0;
		
		@Override
		public String toString() {
			return "{ fixedSize: "+fixedSize+", relativeSize: "+relativeSize+", location: "+location+", size: "+size+" }";
		}
	}
	
	public static class Constraint {
		/** Row or column index this grid constraint's leading edge is attached to */
		public int index = 0;
		/** How many rows or columns this grid constraint reaches across */
		public int span = 1;
		
		public int fixedSize = 0;
		public int relativeSize = 0;
	}
}
