/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
/**
 * SimpleStandardMBean.java
 *
 * Created on Sat Jul 02 01:54:54 PDT 2005
 */
package testmbeans;

/**
 * Interface SimpleStandardMBean
 * SimpleStandard Description
 * @author kedarm
 */
public interface SimpleStandardMBean
{
   /**
    * Get This is the Color Attribute.
    */
    public String getColor();

   /**
    * Set This is the Color Attribute.
    */
    public void setColor(String value);

   /**
    * Get This is the State Attribute
    */
    public boolean getState();

   /**
    * Set This is the State Attribute
    */
    public void setState(boolean value);

   /**
    * Greets someone
    *
    * @param name <code>String</code> The person to greet
    */
    public void greet(String name);

}
