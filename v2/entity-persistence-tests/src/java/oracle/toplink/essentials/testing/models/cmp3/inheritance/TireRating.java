package oracle.toplink.essentials.testing.models.cmp3.inheritance;

import javax.persistence.Embeddable;

@Embeddable
public class TireRating {
    protected String comments;
    protected String rating;
    
    public TireRating() {}
    
    public String getComments() {
        return comments;
    }
    
    public String getRating() {
        return rating;
    }
    
    public void setRating(String rating) {
        this.rating =rating;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
}
