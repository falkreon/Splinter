package blue.endless.splinter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Layout {
	/**
	 * Sets layout parameters for each component and notifies the container of its arbitrated geometry. The container and its
	 * components will be repositioned to fit in (x,y)..(x+width-1,y+height-1), inclusive. If any of the layoutChildren are
	 * themselves LayoutContainers, they will be recursively solved.
	 * @param container The container to layout.
	 * @param x The lowest X coordinate of valid layout space.
	 * @param y The lowest Y coordinate of valid layout space.
	 * @param width The width of valid layout space.
	 * @param height The height of valid layout space.
	 * @param removeCollisions If true, and elements are found to overlap, arbitrary elements will be removed from the
	 *        generated layout to prevent the overlap.
	 */
	public static void layout(LayoutContainer container, int x, int y, int width, int height, boolean removeCollisions) {
		//Compile the LayoutData for this container
		Map<LayoutElement, LayoutElementMetrics> elementData = new HashMap<>();
		GridMetrics gridMetrics = new GridMetrics(); //TODO: Could we keep this as a static field, and clear and reuse this between layouts?
		
		int maxX = 0;
		int maxY = 0;
		for(LayoutElement elem : container.getLayoutChildren()) {
			LayoutElementMetrics metrics = container.getLayoutElementMetrics(elem);
			elementData.put(elem, metrics);
			maxX = Math.max(maxX, metrics.cellX+(metrics.cellsX-1));
			maxY = Math.max(maxY, metrics.cellY+(metrics.cellsY-1));
		}
		
		int gridWidth = maxX+1;
		int gridHeight= maxY+1;
		Set<LayoutElement> removed = new HashSet<>();
		
		if (removeCollisions) {
			LayoutElement[] collisionMap = new LayoutElement[gridWidth*gridHeight];
			for(Map.Entry<LayoutElement, LayoutElementMetrics> entry : elementData.entrySet()) {
				LayoutElement elem = entry.getKey();
				LayoutElementMetrics entryData = entry.getValue();
				
				for(int yi=0; yi<entryData.cellsY; yi++) {
					int elemY = yi + entryData.cellY;
					for(int xi=0; xi<entryData.cellsX; xi++) {
						int elemX = xi + entryData.cellX;
						
						int index = (elemY*gridWidth+elemX) % collisionMap.length;
						LayoutElement existing = collisionMap[index];
						if (existing!=null) {
							removed.add(elem);
						} else {
							collisionMap[index] = elem;
						}
					}
				}
			}
			for(LayoutElement elem : removed) {
				elementData.remove(elem);
			}
		}
		
		//X coordinate of the start of each column
		ArrayList<Integer> columnStart = new ArrayList<>();
		//Dimensions for each column
		ArrayList<Integer> columnSize = new ArrayList<>();
		
		for(int i=0; i<gridWidth; i++) { columnStart.add(0); columnSize.add(0); }
		
		//X coordinate of the start of each column
		ArrayList<Integer> rowStart = new ArrayList<>();
		//Dimensions for each column
		ArrayList<Integer> rowSize = new ArrayList<>();
		
		for(int i=0; i<gridHeight; i++) { rowStart.add(0); rowSize.add(0); }
		
		
		for(Map.Entry<LayoutElement, LayoutElementMetrics> entry : elementData.entrySet()) {
			//FOR NOW, treat multi-column constraints as single-column constraints just to get us up and running with anything
			int existingX = columnSize.get(entry.getValue().cellX);
			int existingY = rowSize.get(entry.getValue().cellY);
			
			int minX = max(
					entry.getKey().getNaturalWidth(),
					entry.getValue().fixedMinX,
					entry.getValue().relativeMinX*width,
					existingX);
			if (minX<0) minX=0;
			
			int minY = max(
					entry.getKey().getNaturalHeight(),
					entry.getValue().fixedMinY,
					(int)((entry.getValue().relativeMinY/100.0)*height),
					existingY);
			if (minY<0) minY=0;
			
			columnSize.set(entry.getValue().cellX, minX);
			rowSize.set(entry.getValue().cellY, minY);
		}
		
		//Sum up the columns and figure out how much we're over or under by, then distribute the overage/shortfall
		//evenly between the columns
		int leftoverX = width;
		for(int i=0; i<columnSize.size(); i++) {
			leftoverX -= columnSize.get(i);
		}
		int leftoverPerColumn = leftoverX / gridWidth;
		for(int i=0; i<columnSize.size(); i++) {
			columnSize.set(i, columnSize.get(i) + leftoverPerColumn);
		}
		
		int leftoverY = height;
		for(int i=0; i<rowSize.size(); i++) {
			leftoverY -= rowSize.get(i);
		}
		int leftoverPerRow = leftoverY / gridHeight;
		for(int i=0; i<rowSize.size(); i++) {
			rowSize.set(i, rowSize.get(i) + leftoverPerRow);
		}
		
		//Set the actual row/column start positions now that we know their sizes
		int curX = 0;
		for(int i=0; i<gridWidth; i++) {
			columnStart.set(i, curX);
			curX += columnSize.get(i);
		}
		
		int curY = 0;
		for(int i=0; i<gridHeight; i++) {
			rowStart.set(i, curY);
			curY += rowSize.get(i);
		}
		
		//If there's any shortfall left, add it to the last row or column.
		int rightmostWidth = columnSize.get(gridWidth-1);
		int rightmostStart = columnStart.get(gridWidth-1);
		if (rightmostStart+rightmostWidth < width) {
			rightmostWidth = (width)-rightmostStart;
			columnSize.set(gridWidth-1, rightmostWidth);
		}
		
		
		int bottommostHeight = rowSize.get(gridHeight-1);
		int bottommostStart = rowStart.get(gridHeight-1);
		if (bottommostStart+bottommostHeight < height) {
			bottommostHeight = (height)-bottommostStart;
			rowSize.set(gridHeight-1, bottommostHeight);
		}
		
		
		container.setGridMetrics(rowStart, rowSize, columnStart, columnSize);
		
		//Notify container of each component's geometry
		for(Map.Entry<LayoutElement, LayoutElementMetrics> entry : elementData.entrySet()) {
			LayoutElement elem = entry.getKey();
			LayoutElementMetrics metrics = entry.getValue();
			if (metrics.cellX<0 || metrics.cellX>=columnStart.size() || metrics.cellY<0 || metrics.cellY>=rowStart.size()) {
				//TODO: Do we want these values onscreen? Do we want to notify a component explicitly that layout has elected to zero it and hide it?
				removed.add(elem);
			} else {
				int cellX = columnStart.get(metrics.cellX);
				int cellY = rowStart.get(metrics.cellY);
				int cellWidth = columnSize.get(metrics.cellX);
				int cellHeight = rowSize.get(metrics.cellY);
				//TODO: This merely defines the available space within which the component may be placed. Here, maximum values can be considered and the element can be aligned against its cell.
				
				container.setLayoutValues(elem, cellX, cellY, cellWidth, cellHeight);
				if (elem instanceof LayoutContainer) {
					layout((LayoutContainer)elem, cellX, cellY, cellWidth, cellHeight, removeCollisions);
				}
			}
		}
		
		for(LayoutElement elem : removed) {
			//TODO: Do we want these values onscreen? Do we want to notify a component explicitly that layout has elected to zero it and hide it?
			container.setLayoutValues(elem, 0, 0, 0, 0);
		}
	}
	
	private static int max(int a, int b, int c, int d) {
		return Math.max(Math.max(Math.max(a, b), c), d);
	}
}
