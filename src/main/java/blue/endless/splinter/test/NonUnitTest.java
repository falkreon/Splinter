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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import blue.endless.splinter.Layout;
import blue.endless.splinter.data.Alignment;
import blue.endless.splinter.data.GrowType;

public class NonUnitTest {
	
	public static void main(String... args) {
		/*//Test resembling Chipper main menu
		final Rectangle rootPanel = new Rectangle();
		rootPanel.getLayoutContainerMetrics().setCellPadding(4);
		rootPanel.backgroundColor = Color.RED;
		rootPanel.borderColor = Color.RED;
		
		
		Rectangle logoContainer = new Rectangle();
		rootPanel.add(logoContainer, 0, 0);
		rootPanel.getMetrics(logoContainer).setMinimumHeight(64);
		rootPanel.getMetrics(logoContainer).setMinimumWidth(64);
		rootPanel.getMetrics(logoContainer).horizontalGrowType = GrowType.PACK;
		rootPanel.getMetrics(logoContainer).horizontalAlignment = Alignment.CENTER;
		
		Rectangle spacer = new Rectangle();
		spacer.borderColor = new Color(0,0,0,0);
		spacer.backgroundColor = new Color(0,0,0,0);
		rootPanel.add(spacer, 0, 1);
		
		for(int i=0; i<4; i++) {
			Rectangle rect = new Rectangle();
			rect.borderColor = Color.BLUE;
			rect.backgroundColor = Color.DARK_GRAY;
			rootPanel.add(rect, 0, 2+i);
			rootPanel.getMetrics(rect).setMinimumHeight(128);
			if (i==3) {
				rootPanel.getMetrics(rect).fixedMinX = 256;
				rootPanel.getMetrics(rect).horizontalGrowType = GrowType.PACK;
				rootPanel.getMetrics(rect).horizontalAlignment = Alignment.LEADING;
				rootPanel.getMetrics(rect).paddingLeft = 128;
			}
		}*/
		
		
		final Rectangle rootPanel = new Rectangle();
		rootPanel.getLayoutContainerMetrics().setCellPadding(16);
		rootPanel.backgroundColor = new Color(0,0,0,0);
		rootPanel.borderColor = new Color(0,0,0,0);
		
		Rectangle test1 = new Rectangle();
		rootPanel.add(test1, 0, 0);
		//rootPanel.getMetrics(test1).setMinPercentHeight(25);
		
		Rectangle test2 = new Rectangle();
		rootPanel.add(test2, 0, 1);
		rootPanel.getMetrics(test2).cellsY = 2;
		rootPanel.getMetrics(test2).relativeMinY = 25;
		
		Rectangle test3 = new Rectangle();
		rootPanel.add(test3, 1, 2);
		rootPanel.getMetrics(test3).cellsY =1;
		
		
		
		@SuppressWarnings("serial")
		JFrame layoutHost = new JFrame() {
			{
				LayoutContainer container = new LayoutContainer();
				container.rootPanel = rootPanel;
				this.setContentPane(container);
			}
		};
		layoutHost.setMinimumSize(new Dimension(600, 600));
		layoutHost.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		layoutHost.setVisible(true);
	}
	
	private static class LayoutContainer extends JPanel {
		private static final long serialVersionUID = -6448369368726067468L;
		public Rectangle rootPanel;
		public int scale = 1;
		
		@Override
		public void paint(Graphics g) {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			
			BufferedImage frame = new BufferedImage(this.getWidth()/scale, this.getHeight()/scale, BufferedImage.TYPE_INT_ARGB);
			rootPanel.setOwnLayoutValues(0, 0, frame.getWidth(), frame.getHeight());
			Layout.layout(rootPanel, 0, 0, frame.getWidth(), frame.getHeight(), false);
			Graphics g2 = frame.getGraphics();
			rootPanel.paint(g2);
			g2.dispose();
			
			g.drawImage(frame, 0, 0, frame.getWidth()*scale, frame.getHeight()*scale, this);
		}
	}
}
