package sort.external;

class ReaderElem<T> {

	public final int idx;
	public final T value;

	public ReaderElem(int idx, T value) {
		super();
		this.idx = idx;
		this.value = value;
	}

	@Override
	public String toString() {
		return value.toString();
	}
}