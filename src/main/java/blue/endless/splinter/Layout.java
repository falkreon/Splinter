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
		
		//int maxX = 0;
		//int maxY = 0;
		for(LayoutElement elem : container.getLayoutChildren()) {
			LayoutElementMetrics metrics = container.getLayoutElementMetrics(elem);
			int minWidth = elem.getNaturalWidth();
			if (minWidth>0) metrics.fixedMinX = Math.max(metrics.fixedMinX, minWidth);
			int minHeight = elem.getNaturalHeight();
			if (minHeight>0) metrics.fixedMinY = Math.max(metrics.fixedMinY, minHeight);
			
			elementData.put(elem, metrics);
			gridMetrics.addElementMetrics(metrics);
		}

		Set<LayoutElement> removed = new HashSet<>();
		
		/*
		if (removeCollisions) {
			LayoutElement[] collisionMap = new LayoutElement[gridMetrics.width*gridMetrics.height];
			for(Map.Entry<LayoutElement, LayoutElementMetrics> entry : elementData.entrySet()) {
				LayoutElement elem = entry.getKey();
				LayoutElementMetrics entryData = entry.getValue();
				
				for(int yi=0; yi<entryData.cellsY; yi++) {
					int elemY = yi + entryData.cellY;
					for(int xi=0; xi<entryData.cellsX; xi++) {
						int elemX = xi + entryData.cellX;
						
						int index = (elemY*gridMetrics.width+elemX) % collisionMap.length;
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
		}*/
		
		//Set initial sizes
		setInitial(gridMetrics.xMetrics, gridMetrics.width, width);
		setInitial(gridMetrics.yMetrics, gridMetrics.height, height);
		
		/*
		for(GridMetrics.Element elem : gridMetrics.xMetrics) {
			elem.size = Math.max(elem.fixedSize, (int)((elem.relativeSize/100.0)*width));
		}
		
		for(GridMetrics.Element elem : gridMetrics.yMetrics) {
			elem.size = Math.max(elem.fixedSize, (int)((elem.relativeSize/100.0)*height));
		}*/
		
		//Preferentially grow elements that don't have a specific size declared
		stretchUnspecified(gridMetrics.xMetrics, gridMetrics.width, width);
		stretchUnspecified(gridMetrics.yMetrics, gridMetrics.height, height);
		
		
		//Stretch all elements equally
		int leftoverX = width;
		for(int i=0; i<gridMetrics.width; i++) {
			leftoverX -= gridMetrics.getCellWidth(i);
		}
		int leftoverPerColumn = leftoverX / gridMetrics.width;
		for(int i=0; i<gridMetrics.width; i++) {
			gridMetrics.xMetrics[i].size += leftoverPerColumn;
		}
		
		int leftoverY = height;
		for(int i=0; i<gridMetrics.height; i++) {
			leftoverY -= gridMetrics.getCellHeight(i);
		}
		int leftoverPerRow = leftoverY / gridMetrics.height;
		for(int i=0; i<gridMetrics.height; i++) {
			gridMetrics.yMetrics[i].size += leftoverPerRow;
		}
		
		/*
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
		}*/
		
		gridMetrics.recalcStarts();
		
		//If there's any shortfall left, add it to the last row or column.
		GridMetrics.Element rightmost = gridMetrics.getColumns()[gridMetrics.getWidth()-1];
		if (rightmost.location+rightmost.size < width) {
			rightmost.size = (width)-rightmost.location;
		}
		
		GridMetrics.Element bottommost = gridMetrics.getRows()[gridMetrics.getHeight()-1];
		if (bottommost.location+bottommost.size < height) {
			bottommost.size = (height)-bottommost.location;
		}
		
		//container.setGridMetrics(rowStart, rowSize, columnStart, columnSize);
		//if (gridMetrics.height==3) System.out.println(gridMetrics);
		
		//Notify container of each component's geometry
		for(Map.Entry<LayoutElement, LayoutElementMetrics> entry : elementData.entrySet()) {
			LayoutElement elem = entry.getKey();
			LayoutElementMetrics metrics = entry.getValue();
			if (metrics.cellX<0 || metrics.cellY<0) {
				//TODO: Do we want these values onscreen? Do we want to notify a component explicitly that layout has elected to zero it and hide it?
				removed.add(elem);
			} else {
				/*
				int cellX = columnStart.get(metrics.cellX);
				int cellY = rowStart.get(metrics.cellY);
				int cellWidth = columnSize.get(metrics.cellX);
				int cellHeight = rowSize.get(metrics.cellY);*/
				//TODO: This merely defines the available space within which the component may be placed. Here, maximum values can be considered and the element can be aligned against its cell.
				int cellX = gridMetrics.getCellLeft(metrics.cellX);
				int cellY = gridMetrics.getCellTop(metrics.cellY);
				int cellWidth = gridMetrics.getCellWidth(metrics.cellX);
				int cellHeight = gridMetrics.getCellHeight(metrics.cellY);
				
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
	
	private static void setInitial(GridMetrics.Element[] elements, int num, int totalSize) {
		for(int i=0; i<num; i++) {
			GridMetrics.Element elem = elements[i];
			
			elem.size = Math.max(elem.fixedSize, (int)((elem.relativeSize/100.0)*totalSize));
		}
	}
	
	private static int stretchUnspecified(GridMetrics.Element[] elements, int num, int totalSize) {
		int leftover = totalSize;
		int unspecified = 0;
		for(int i=0; i<num; i++) {
			GridMetrics.Element elem = elements[i];
			//System.out.println("Elem "+elem.size);
			leftover-= elem.size;
			if (elem.fixedSize<=0 && elem.relativeSize<=0) unspecified++;
		}
		if (leftover<=0) return -leftover;
		if (unspecified==0) return 0;
		int leftoverPerElem = leftover / unspecified;
		for(int i=0; i<num; i++) {
			GridMetrics.Element elem = elements[i];
			if (elem.fixedSize<=0 && elem.relativeSize<=0) elem.size += leftoverPerElem;
		}
		
		return 0;
	}
}
