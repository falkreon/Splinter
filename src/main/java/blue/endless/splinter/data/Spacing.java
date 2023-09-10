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

package blue.endless.splinter.data;

/**
 * Represents some spacing around each edge of a two-dimensional box, such as a margin,
 * border, padding, or cell-padding.
 */
public record Spacing(Size top, Size right, Size bottom, Size left) {
	public static final Spacing NONE = of(Size.pixels(0));
	public static final Spacing DEFAULT_MARGIN = of(Size.pixels(4));
	public static final Spacing DEFAULT_PADDING = of(Size.pixels(8));
	
	public Size leading(Axis axis) {
		return (axis == Axis.HORIZONTAL) ? left : top;
	}
	
	public Size trailing(Axis axis) {
		return (axis == Axis.HORIZONTAL) ? right : bottom;
	}
	
	public static Spacing of(Size s) {
		return new Spacing(s, s, s, s);
	}
}
