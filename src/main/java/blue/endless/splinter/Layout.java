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
		LayoutContainerMetrics containerMetrics = container.getLayoutContainerMetrics();
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
		setInitial(gridMetrics.xMetrics, gridMetrics.width, width, containerMetrics);
		setInitial(gridMetrics.yMetrics, gridMetrics.height, height, containerMetrics);
		
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
		
		
		gridMetrics.recalcStarts();
		
		stretchEnd(gridMetrics.xMetrics, gridMetrics.width, width);
		stretchEnd(gridMetrics.yMetrics, gridMetrics.height, height);
		/*
		//If there's any shortfall left, add it to the last row or column.
		GridMetrics.Element rightmost = gridMetrics.getColumns()[gridMetrics.getWidth()-1];
		if (rightmost.location+rightmost.size < width) {
			rightmost.size = (width)-rightmost.location;
		}
		
		GridMetrics.Element bottommost = gridMetrics.getRows()[gridMetrics.getHeight()-1];
		if (bottommost.location+bottommost.size < height) {
			bottommost.size = (height)-bottommost.location;
		}*/
		
		container.setGridMetrics(gridMetrics);
		//if (gridMetrics.height>3) System.out.println(gridMetrics);
		
		//Notify container of each component's geometry
		for(Map.Entry<LayoutElement, LayoutElementMetrics> entry : elementData.entrySet()) {
			LayoutElement elem = entry.getKey();
			LayoutElementMetrics metrics = entry.getValue();
			if (metrics.cellX<0 || metrics.cellY<0) {
				//TODO: Do we want these values onscreen? Do we want to notify a component explicitly that layout has elected to zero it and hide it?
				removed.add(elem);
			} else {
				//This merely defines the available space within which the component may be placed. Here, maximum values can be considered and the element can be aligned against its cell.
				int cellX = x + gridMetrics.getCellLeft(metrics.cellX);
				int cellY = y + gridMetrics.getCellTop(metrics.cellY);
				int cellWidth = gridMetrics.getCellWidth(metrics.cellX);
				int cellHeight = gridMetrics.getCellHeight(metrics.cellY);
				
				int paddingLeft = containerMetrics.cellPadding; if (metrics.cellX>0) paddingLeft /= 2;
				int paddingTop = containerMetrics.cellPadding; if (metrics.cellY>0) paddingTop /= 2;
				int paddingRight = containerMetrics.cellPadding; if (metrics.cellX<gridMetrics.width-1) paddingRight /= 2;
				int paddingBottom = containerMetrics.cellPadding; if (metrics.cellY<gridMetrics.height-1) paddingBottom /= 2;
				
				if (containerMetrics.collapseMargins) {
					paddingLeft = Math.max(metrics.paddingLeft, paddingLeft);
					paddingTop = Math.max(metrics.paddingTop, paddingTop);
					paddingRight = Math.max(metrics.paddingRight, paddingRight);
					paddingBottom = Math.max(metrics.paddingBottom, paddingBottom);
				} else {
					paddingLeft += metrics.paddingLeft;
					paddingTop += metrics.paddingTop;
					paddingRight += metrics.paddingRight;
					paddingBottom += metrics.paddingBottom;
				}
				
				//These ones are actually the maximum-sized element within the cell
				int elemX = cellX + paddingLeft;
				int elemY = cellY + paddingTop;
				int elemWidth = cellWidth - (paddingLeft+paddingRight);
				int elemHeight = cellHeight - (paddingTop+paddingBottom);
				
				if (metrics.horizontalGrowType==GrowType.PACK) {
					int preferredWidth = Math.max(metrics.fixedMinX, (int)((metrics.relativeMinX/100.0)*width));
					if (preferredWidth<elemWidth) {
						elemWidth = preferredWidth;
						switch (metrics.horizontalAlignment) {
						case CENTER: {
							int offset = (cellWidth/2) - (preferredWidth/2);
							elemX += offset;
							break;
						}
						case TRAILING: {
							int offset = (cellWidth-(paddingLeft+paddingRight)) - preferredWidth;
							elemX += offset;
							break;
						}
						case LEADING:
						default: //Do nothing
							break;
						}
					}
				}
				
				if (metrics.verticalGrowType==GrowType.PACK) {
					int preferredHeight = Math.max(metrics.fixedMinY, (int)((metrics.relativeMinY/100.0)*height));
					if (preferredHeight<elemHeight) {
						elemHeight = preferredHeight;
						switch (metrics.verticalAlignment) {
						case CENTER: {
							int offset = (cellHeight/2) - (preferredHeight/2);
							elemY += offset;
							break;
						}
						case TRAILING: {
							int offset = (cellHeight-(paddingTop+paddingBottom)) - preferredHeight;
							elemY += offset;
							break;
						}
						case LEADING:
						default: //Do nothing
							break;
						}
					}
				}
				
				container.setLayoutValues(elem, elemX, elemY, elemWidth, elemHeight);
				if (elem instanceof LayoutContainer) {
					layout((LayoutContainer)elem, elemX, elemY, elemWidth, elemHeight, removeCollisions);
				}
			}
		}
		
		for(LayoutElement elem : removed) {
			//TODO: Do we want these values onscreen? Do we want to notify a component explicitly that layout has elected to zero it and hide it?
			container.setLayoutValues(elem, 0, 0, 0, 0);
		}
	}
	
	
	
	private static void setInitial(GridMetrics.Element[] elements, int num, int totalSize, LayoutContainerMetrics containerMetrics) {
		for(int i=0; i<num; i++) {
			GridMetrics.Element elem = elements[i];
			
			elem.size = Math.max(elem.fixedSize+(containerMetrics.cellPadding*2), (int)((elem.relativeSize/100.0)*totalSize)+(containerMetrics.cellPadding*2));
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
	
	private static void stretchEnd(GridMetrics.Element[] elements, int num, int totalSize) {
		GridMetrics.Element rightmost = elements[num-1];
		if (rightmost.location+rightmost.size < totalSize) {
			rightmost.size = (totalSize)-rightmost.location;
		}
	}
}
