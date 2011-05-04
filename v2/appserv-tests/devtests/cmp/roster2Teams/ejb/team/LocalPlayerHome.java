/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 *
 */

package team;

import java.util.*;
import javax.ejb.*;

public interface LocalPlayerHome extends EJBLocalHome {
    
    public LocalPlayer create (String id, String name, String position,
        double salary)
        throws CreateException;
    
    public LocalPlayer findByPrimaryKey (String id)
        throws FinderException;
    
    public Collection findByPosition(String position) 
        throws FinderException;

    public Collection findByHigherSalary(String name) 
        throws FinderException;

    public Collection findBySalaryRange(double low, double high) 
        throws FinderException;

    public Collection findByLeague(LocalLeague league) 
        throws FinderException;

    public Collection findBySport(String sport) 
        throws FinderException;

    public Collection findByCity(String city) 
        throws FinderException;

    public Collection findAll() 
        throws FinderException;

    public Collection findNotOnTeam() 
        throws FinderException;

    public Collection findByPositionAndName(String position, 
        String name) throws FinderException;

    public Collection findByTest (String parm1, String parm2, String parm3)
        throws FinderException;

    public Collection findByPositionsGoalkeeperOrDefender()
        throws FinderException;   	
        
    public Collection findByNameEndingWithON()
        throws FinderException;   	

    public Collection findByNullName()
        throws FinderException;   	

    public Collection findByTeam(LocalTeam team)
        throws FinderException;   	        
        
    public Collection findBySalarayWithArithmeticFunctionABS(double salaray)
        throws FinderException;       

    public Collection findBySalarayWithArithmeticFunctionSQRT(double salaray)
        throws FinderException;       
        
}
