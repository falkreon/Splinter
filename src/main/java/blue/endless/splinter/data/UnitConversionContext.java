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

public record UnitConversionContext(double pixelsPerEm, double pixelsPerPoint, int sizeAvailable) implements Size.Converter {
	public Size convert(Size size, SizeUnit targetUnit) {
		//Convert size down to pixels
		
		double pixelValue = switch(size.units()) {
			case PIXELS -> size.value();
			case PERCENT -> (size.value() / 100.0) * sizeAvailable;
			case EMS -> size.value() * pixelsPerEm;
			case POINTS -> size.value() * pixelsPerPoint;
		};
		
		//Convert pixelValue up to target
		
		double result = switch(targetUnit) {
			case PIXELS -> pixelValue;
			case PERCENT -> (pixelValue / sizeAvailable) * 100.0;
			case EMS -> pixelValue / pixelsPerEm;
			case POINTS -> pixelValue / pixelsPerPoint;
		};
		
		//Round and package
		
		return new Size((int) result, targetUnit);
	}
}
