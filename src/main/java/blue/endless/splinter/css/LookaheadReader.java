package blue.endless.splinter.css;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

public class LookaheadReader {
	private final Reader in;
	private boolean eof = false;
	/**
	 * Lookahead / pushback buffer for the reader. "First" is conceptually on the *left* and indicates the buffered
	 * character that will be returned *last* out of all the buffered characters. "Last" is conceptually on the *right*,
	 * and in a non-empty buffer, will be the very next character returned by read(). As such, pushback always pushes
	 * characters back onto the *right* of the stream.
	 * 
	 * <p>This means, conceptually, Strings are seen *backwards* in the buffer- "!dlroW olleH" will read the 'H' first,
	 * and then the 'e', and so on, to reconstruct "Hello World!" forwards when read. So don't get confused!
	 * 
	 */
	private LinkedList<Integer> lookahead = new LinkedList<>();

	public LookaheadReader(Reader reader) {
		this.in = reader;
	}
	
	public void close() throws IOException {
		in.close();
	}

	public int read() throws IOException {
		if (lookahead.isEmpty()) increaseLookahead();
		
		return lookahead.removeLast();
	}
	
	public String read(int numChars) throws IOException {
		StringBuilder result = new StringBuilder();
		
		for(int i=0; i<numChars; i++) {
			int ch = read();
			if (eof || ch==-1) return result.toString();
			
			result.append((char) ch);
		}
		
		return result.toString();
	}
	
	public void pushback(int ch) {
		if (ch == 0) throw new IllegalArgumentException("Can't pushback invalid values.");
		if (ch < 0) return; //Ignore -1's
		
		lookahead.addLast(ch);
	}
	
	public void pushback(String s) {
		//in order to be read in-order, characters need to be pushed right-to-left back onto the stream.
		for(int i=s.length()-1; i>=0; i--) {
			lookahead.addLast((int) s.charAt(i));
		}
	}
	
	//Handles filtering for single characters
	private int filterSingle(int in) {
		return switch(in) {
		case 0x0000 -> 0xFFFD;
		case 0x000D -> (int) '\n';
		case 0x000C -> (int) '\n';
		default -> in;
		};
	}
	
	private void increaseLookahead() throws IOException {
		int i = in.read();
		
		if (i == -1) {
			eof = true;
			lookahead.addFirst(-1); //Hopefully we can get rid of this, but just for safety's sake we'll leave it for now.
		}
		
		/*
		 * We're required to make the following newline replacements:
		 * \f (0x0C) -> \n
		 * \r (0x0D) -> \n
		 * \r\n -> \n
		 * 
		 * This means all we see at the consumer end is '\n'. This is very similar to the BufferedReader logic.
		 */
		if (i == 0x000D) { //This is a cr. Look for cr+lf
			int j = in.read();
			if (j == 0x000A) {
				// It was indeed a cr+lf, so replace those with a single lf
				lookahead.addFirst((int) '\n');
			} else {
				// It was a cr and then a non-lf character, so let both characters pass, filtered.
				lookahead.addFirst((int) '\n');
				lookahead.addFirst(filterSingle(j));
			}
		} else {
			lookahead.addFirst(filterSingle(i));
		}
	}
	
	/**
	 * Looks at the next character in the stream. Returns the next character that will be returned by {@link #read()}
	 * @return the next character that will be returned by {@link #read()}, or -1 if the end of the stream has been
	 * reached.
	 */
	public int peek() throws IOException {
		if (lookahead.isEmpty()) increaseLookahead();
		
		return lookahead.peekLast();
	}
	
	/**
	 * peekFurther(0) returns the same value as peek() - the next element in the stream. peekFurther(1) returns the
	 * value that will be read after the next value.
	 * @param numChars The number of characters to look ahead
	 * @return the value that many characters into the future.
	 * @throws IOException if there was an error reading characters.
	 */
	public int peekFurther(int numChars) throws IOException {
		if (numChars < 0) throw new IllegalArgumentException("Cannot look a negative number of characters ahead.");
		while (lookahead.size() < numChars && !eof) increaseLookahead();
		
		int index = lookahead.size() - 1 - numChars;
		return (index < 0) ? -1 : lookahead.get(index);
	}
	
	/**
	 * Looks at the next n characters in the stream. Returns what would be returned by a {@link #read(int)} with the
	 * same character count.
	 * @param numChars the number of lookahead characters to inspect
	 * @return The next n characters in the stream. If the returned String is length zero, or its length is otherwise
	 * less than n, the end of the stream was reached.
	 * @throws IOException if there was an error reading characters.
	 */
	public String peek(int numChars) throws IOException {
		while (lookahead.size() < numChars && !eof) increaseLookahead();
		
		StringBuilder result = new StringBuilder();
		for(int i=0; i<numChars; i++) {
			int codePoint = lookahead.get(lookahead.size() - 1 - i);
			result.append((char) codePoint);
		}
		return result.toString();
	}
	
	public boolean eof() {
		return eof;
	}
	
}
