/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.samples.javaone2011;

import javax.ejb.Stateful;
import javax.enterprise.inject.Model;

/**
 *
 * @author arungup
 */
@Stateful
@Model
public class MoviePreferencesBean {

    private String[] selectedMovies;

    public String[] getSelectedMovies() {
        return selectedMovies;
    }
    
    public void setSelectedMovies(String[] movies) {
        selectedMovies = movies;
    }

}
