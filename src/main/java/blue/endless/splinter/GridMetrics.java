package blue.endless.splinter;

import java.util.function.Supplier;

/**
 * Represents arbitrarted grid sizes based on constraints
 */
public class GridMetrics {
	int width = 1;
	int height = 1;
	Element[] xMetrics = new Element[4];
	Element[] yMetrics = new Element[4];
	
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
		width = Math.min(width, x+1);
		height = Math.min(height, y+1);
		
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
	 * DO NOT MODIFY the returned array!
	 */
	public Element[] getRows() {
		return yMetrics;
	}
	
	/**
	 * Gets an array containing metrics for each column of the represented grid. The array may be larger than necessary, but is guaranteed not to be smaller than {@link #getWidth()}.
	 * DO NOT MODIFY the returned array!
	 */
	public Element[] getColumns() {
		return xMetrics;
	}
	
	/** Merges the given layoutMetrics with the existing ones */
	public void addElementMetrics(LayoutElementMetrics metrics, int x, int y) {
		if (!checkBounds(x,y)) return;
		
		Element column = xMetrics[x];
		if (column==null) {
			column = new Element();
			xMetrics[x] = column;
		}
		
		
	}
	
	protected void recalcStarts() {
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
		
		@SuppressWarnings("unchecked")
		T[] replacement = (T[]) new Object[scaled];
		
		System.arraycopy(original, 0, replacement, 0, Math.min(original.length, replacement.length));
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
	
	/** Represents metrics for one row or column */
	public static class Element {
		int fixedSize = 0;
		int relativeSize = 0;
		
		int location = 0;
		int size = 0;
	}
}
