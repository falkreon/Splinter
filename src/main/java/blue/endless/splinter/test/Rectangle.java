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
		
		for(Widget child : oldChildren.keySet()) {
			if (child instanceof Paintable) {
				((Paintable)child).paint(g);
			}
		}
	}
}
