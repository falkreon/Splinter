/*
 * MIT License
 *
 * Copyright (c) 2019 Falkreon (Isaac Ellingson)
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
