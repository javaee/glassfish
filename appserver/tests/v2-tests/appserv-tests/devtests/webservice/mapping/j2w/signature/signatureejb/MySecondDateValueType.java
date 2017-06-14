package signatureejb;

import java.util.Date;

public class MySecondDateValueType {
    protected java.util.Date date;
    protected String whine;

    public MySecondDateValueType() {
    }

    public MySecondDateValueType(Date date, java.lang.String whine) {
        this.date = date;
        this.whine = whine;
    }

    public java.util.Date getDate() {
        return date;
    }

    public void setDate(java.util.Date date) {
        this.date = date;
    }

    public java.lang.String getWhine() {
        return whine;
    }

    public void setWhine(java.lang.String whine) {
        this.whine = whine;
    }
}
