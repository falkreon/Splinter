package blue.endless.splinter.widget;

import blue.endless.splinter.LayoutElement;

public class Widget implements LayoutElement {
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	
	public void setOwnLayoutValues(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}
