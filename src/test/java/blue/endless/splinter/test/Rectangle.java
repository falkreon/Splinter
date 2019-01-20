package blue.endless.splinter.test;

import java.awt.Color;
import java.awt.Graphics;

import blue.endless.splinter.widget.ContainerWidget;
import blue.endless.splinter.widget.Widget;

public class Rectangle extends ContainerWidget implements Paintable {
	public int borderWidth = 8;
	public Color borderColor = Color.BLUE;
	public Color backgroundColor = Color.LIGHT_GRAY;
	
	@Override
	public void paint(Graphics g) {
		g.setColor(backgroundColor);
		g.fillRect(x, y, width, height);
		
		if (borderWidth>0) {
			g.setColor(borderColor);
			g.fillRect(x, y, width, borderWidth);
			g.fillRect(x, y+height-borderWidth, width, borderWidth);
			g.fillRect(x, y+borderWidth, borderWidth, height-(borderWidth*2));
			g.fillRect(x+width-(borderWidth), y+borderWidth, borderWidth, height-(borderWidth*2));
		}
		
		for(Widget child : children.keySet()) {
			if (child instanceof Paintable) {
				((Paintable)child).paint(g);
			}
		}
	}
}
