package blue.endless.splinter.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import blue.endless.splinter.Layout;

public class NonUnitTest {
	
	public static void main(String... args) {
		final Rectangle rootPanel = new Rectangle();
		rootPanel.backgroundColor = Color.RED;
		rootPanel.borderColor = Color.RED;
		
		Rectangle test1 = new Rectangle();
		test1.backgroundColor = Color.DARK_GRAY;
		test1.borderColor = Color.RED;
		rootPanel.add(test1, 0, 0);
		rootPanel.getMetrics(test1).setMinPercentHeight(25);
		
		Rectangle a1 = new Rectangle();
		a1.borderColor = new Color(0,0,0,0);
		a1.backgroundColor = new Color(0,0,0,0);
		test1.add(a1, 0, 0);
		
		Rectangle a2 = new Rectangle();
		a2.borderColor = Color.CYAN;
		a2.backgroundColor = Color.BLACK;
		test1.add(a2, 1, 0);
		
		Rectangle a3 = new Rectangle();
		a3.borderColor = new Color(0,0,0,0);
		a3.backgroundColor = new Color(0,0,0,0);
		test1.add(a3, 2, 0);
		
		Rectangle test2 = new Rectangle();
		test2.backgroundColor = Color.DARK_GRAY;
		test2.borderColor = Color.GREEN;
		rootPanel.add(test2, 0, 1);
		rootPanel.getMetrics(test2).setMinPercentHeight(25);
		
		Rectangle test3 = new Rectangle();
		test3.backgroundColor = Color.DARK_GRAY;
		test3.borderColor = Color.BLUE;
		rootPanel.add(test3, 0,2);
		rootPanel.getMetrics(test3).setMinPercentHeight(50);
		
		
		@SuppressWarnings("serial")
		JFrame layoutHost = new JFrame() {
			{
				LayoutContainer container = new LayoutContainer();
				container.rootPanel = rootPanel;
				this.setContentPane(container);
			}
			/*
			public void paint(Graphics g) {
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
				
				rootPanel.setOwnLayoutValues(10, 50, this.getWidth()-20, this.getHeight()-60); //Technically the root container need not be a Widget and could be 0x0; this should not affect Layout
				Layout.layout(rootPanel, 0, 0, this.getWidth(), this.getHeight(), false);
				
				rootPanel.paint(g);
			};*/
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
