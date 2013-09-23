package helper;

import java.util.Iterator;

public class Iter {
	static class IteratorIterable<T> implements Iterable<T>
	{
		private Iterator<T> it;
		
		public IteratorIterable(Iterator<T> it) {
			this.it = it;
		}

		@Override
		public Iterator<T> iterator() {
			return it;
		}
		
		
	}
	
	public static <T> Iterable<T> ate(Iterator<T> it)
	{
		return new IteratorIterable<T>(it);
	}
}
