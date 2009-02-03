package slsbnicmt;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.persistence.*;
import javax.transaction.*;

@Stateless
public class AnnotatedEJB {
    @PersistenceContext
    private EntityManager em;

    private String name = "foo";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean persistEntity(){
        boolean pass = false;
        try {
	  JpaBean jpaBean = new JpaBean();
	  jpaBean.setName("JpaBean");
          System.out.println("Persisting ....");
	  em.persist(jpaBean);
          pass = true;
	} catch (Throwable e) {
           e.printStackTrace();
	}
        return pass;
    }

    public boolean removeEntity(){
        boolean pass = false;
        try {
	  Query query = em.createQuery("SELECT j FROM JpaBean j WHERE j.name='JpaBean'");
	  JpaBean jpaBean = (JpaBean) query.getSingleResult();
	  System.out.println("Loaded " + jpaBean);
	  em.remove(jpaBean);
          pass = true;
	} catch (Throwable e) {
           e.printStackTrace();
	}
        return pass;
    }

    public boolean verifyRemove(){
        boolean pass = false;
        try {
	  Query query = em.createQuery("SELECT count(j) FROM JpaBean j WHERE j.name='JpaBean'");
	  int count = ((Number) query.getSingleResult()).intValue();
	  if (count == 0) {
            pass = true;
	  }
	} catch (Throwable e) {
           e.printStackTrace();
	}
        return pass;
    }

    public String toString() {
        return "AnnotatedEJB[name=" + name + "]";
    }
}
