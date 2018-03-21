package robogp.matchmanager;

public class Pair<K,V> {
	private final K e1;
	private final V e2;

	public Pair(K k, V v) {
		this.e1 = k;
		this.e2 = v;
	}

	public K getFirst() {
		return this.e1;
	}

	public V getSecond() {
		return this.e2;
	}
}