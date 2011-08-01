/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.samples;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author arun
 */
@Stateless
@LocalBean
@Named
public class MovieSessionBean {
    
    @PersistenceContext EntityManager em;
    
    public List<Movie> getMovies() {
        return (List<Movie>)em.createQuery("select m from Movie m").getResultList();
    }
    
    public String getSuggestions(String term) {
        Client client = Client.create();
        client.addFilter(new LoggingFilter());
        WebResource resource = client.resource("http://api.netflix.com/catalog/titles/autocomplete").
                queryParam("term", term).
                queryParam("oauth_consumer_key", "gsksu9qfv8qjrnr2679avr25");
        String response = resource.accept(MediaType.APPLICATION_JSON).get(String.class);
        return response;
    }
    
}
