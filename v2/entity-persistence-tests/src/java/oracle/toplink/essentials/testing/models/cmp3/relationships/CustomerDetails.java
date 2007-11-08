package oracle.toplink.essentials.testing.models.cmp3.relationships;

public class CustomerDetails 
{
    private Integer custId;
    private int orderCount;
    
    public CustomerDetails()
    {    
    }
    
    public CustomerDetails(Integer custId, int orderCount) 
    {
        this.custId = custId;
        this.orderCount = orderCount;
    }
}
