package signatureejb;

import java.util.Date;

public class MyDateValueType {
    protected java.util.Date date;
    protected String whine;
    protected MySecondDateValueType[] dates;

    public MyDateValueType() {}

    public MyDateValueType(Date date, java.lang.String whine, 
                            MySecondDateValueType[] dates) {
        this.date = date;
        this.whine = whine;
        this.dates = dates;
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

    public MySecondDateValueType[] getMySecondDateValueTypes() {
        return dates;
    }

    public void setMySecondDateValueTypes(MySecondDateValueType[] dates) {
        this.dates = dates;
    }
}
