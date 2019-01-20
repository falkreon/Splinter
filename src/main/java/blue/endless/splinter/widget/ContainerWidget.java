package blue.endless.splinter.widget;

import java.util.HashMap;

import blue.endless.splinter.LayoutContainer;
import blue.endless.splinter.LayoutContainerMetrics;
import blue.endless.splinter.LayoutElementMetrics;
import blue.endless.splinter.LayoutElement;

public class ContainerWidget extends Widget implements LayoutContainer {
	protected LayoutContainerMetrics metrics = new LayoutContainerMetrics();
	protected HashMap<Widget, LayoutElementMetrics> children = new HashMap<>();
	
	public void add(Widget w, int x, int y) {
		children.put(w, new LayoutElementMetrics(x,y));
	}
	
	public LayoutElementMetrics getMetrics(Widget w) {
		return children.get(w);
	}
	
	
	
	//implements LayoutContainer {
		@Override
		public Iterable<? extends LayoutElement> getLayoutChildren() {
			return children.keySet();
		}
	
		@Override
		public LayoutElementMetrics getLayoutElementMetrics(LayoutElement elem) {
			if (elem instanceof Widget) {
				return children.get(elem);
			} else {
				return LayoutElementMetrics.EMPTY_METRICS;
			}
		}
	
		@Override
		public LayoutContainerMetrics getLayoutContainerMetrics() {
			return metrics;
		}
	
		@Override
		public void setLayoutValues(LayoutElement elem, int x, int y, int width, int height) {
			if (elem instanceof Widget) {
				((Widget)elem).setOwnLayoutValues(x, y, width, height);
			}
		}
	//}
}
