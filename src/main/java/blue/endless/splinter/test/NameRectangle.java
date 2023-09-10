package blue.endless.splinter.test;

import java.awt.Color;
import java.awt.Graphics;

import blue.endless.splinter.widget.Widget;

public class NameRectangle extends Widget implements Paintable {
	public String text = "";
	public int borderWidth = 8;
	public Color borderColor = Color.BLUE;
	public Color backgroundColor = Color.LIGHT_GRAY;
	
	
	@Override
	public void paint(Graphics g) {
		
	}
	
	@Override
	public int getNaturalWidth() {
		
		// TODO Auto-generated method stub
		return super.getNaturalWidth();
	}
}
