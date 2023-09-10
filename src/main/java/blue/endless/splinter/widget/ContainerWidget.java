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

package blue.endless.splinter.widget;

import java.util.HashMap;

import blue.endless.splinter.LayoutContainer;
import blue.endless.splinter.metrics.LayoutContainerMetrics;
import blue.endless.splinter.metrics.LayoutElementAxisMetrics;
import blue.endless.splinter.metrics.LayoutElementMetrics;
import blue.endless.splinter.metrics.OldLayoutElementMetrics;
import blue.endless.splinter.LayoutElement;
import blue.endless.splinter.data.Axis;

public class ContainerWidget extends Widget implements LayoutContainer {
	protected LayoutContainerMetrics metrics = new LayoutContainerMetrics();
	protected HashMap<Widget, OldLayoutElementMetrics> oldChildren = new HashMap<>();
	protected HashMap<Widget, LayoutElementMetrics> children = new HashMap<>();
	protected int naturalWidth = 0;
	protected int naturalHeight = 0;
	
	public void add(Widget w, int x, int y) {
		oldChildren.put(w, new OldLayoutElementMetrics(x,y));
		children.put(w, new LayoutElementMetrics(x, y));
	}
	
	
	
	//implements LayoutContainer {
		@Override
		public Iterable<? extends LayoutElement> getLayoutChildren() {
			return children.keySet();
		}
		
		@Override
		public OldLayoutElementMetrics getOldLayoutElementMetrics(LayoutElement elem) {
			return oldChildren.get(elem);
		}
		
		@Override
		public LayoutElementAxisMetrics getChildMetrics(LayoutElement elem, Axis axis) {
			return children.get(elem).getAxis(axis);
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

		@Override
		public void setNaturalWidth(int value) {
			this.naturalWidth = value;
		}

		@Override
		public void setNaturalHeight(int value) {
			this.naturalHeight = value;
		}

		@Override
		public int getNaturalWidth() {
			return naturalWidth;
		}

		@Override
		public int getNaturalHeight() {
			return naturalHeight;
		}
		
	//}
}
