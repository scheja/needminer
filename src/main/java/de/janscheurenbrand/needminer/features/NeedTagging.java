package de.janscheurenbrand.needminer.features;

import java.util.Date;
import java.util.List;

/**
 * Created by janscheurenbrand on 03/08/15.
 */
public class NeedTagging {
    private String tagger;
    private Date date;
    private List<Need> needs;
    private int needCount;

    public NeedTagging(String tagger, List<Need> needs) {
        this.tagger = tagger;
        this.needs = needs;
        this.date = new Date();

        // if nothing was selected, we must set needCount to zero
        if (needs == null) {
            this.needCount = 0;
        } else {
            this.needCount = needs.size();
        }
    }

    public NeedTagging() {

    }

    public String getTagger() {
        return tagger;
    }

    public void setTagger(String tagger) {
        this.tagger = tagger;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Need> getNeeds() {
        return needs;
    }

    public void setNeeds(List<Need> needs) {
        this.needs = needs;
    }

    public int getNeedCount() {
        return needCount;
    }

    public void setNeedCount(int needCount) {
        this.needCount = needCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NeedTagging)) return false;

        NeedTagging that = (NeedTagging) o;

        if (!tagger.equals(that.tagger)) return false;
        return date.equals(that.date);
    }

    @Override
    public int hashCode() {
        int result = tagger.hashCode();
        result = 31 * result + date.hashCode();
        return result;
    }
}
