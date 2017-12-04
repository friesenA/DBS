package server;


public enum Branches {
	BC(0),
	MB(1),
	NB(2),
	QC(3);
	
	private final int offset;
    Branches(int offset) { this.offset = offset; }
    public int getValue() { return this.offset; }
}
