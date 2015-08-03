package de.janscheurenbrand.needminer.features;

/**
 * Created by janscheurenbrand on 14/07/15.
 */
public class Need {
    private int start;
    private int end;

    public Need(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Need)) return false;

        Need need = (Need) o;

        if (start != need.start) return false;
        return end == need.end;

    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        return result;
    }
}
