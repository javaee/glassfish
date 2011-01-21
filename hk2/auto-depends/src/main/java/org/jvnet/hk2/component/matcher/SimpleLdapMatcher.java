/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.jvnet.hk2.component.matcher;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jvnet.hk2.component.Constants;
import org.jvnet.hk2.component.MultiMap;

/**
 * This thing filters things based on the input LDAP style string. Note that
 * this is restricted LDAP, in that it only handles "&".
 * 
 * @since 3.1
 */
public class SimpleLdapMatcher {
  private static final char LEFT = '(';

  private static final char RIGHT = ')';

  private static final char AND = '&';

  private static final char OR = '|';

  private static final char EQ = '=';

  private static final char NOT = '!';

  private static final int UNKNOWN_TYPE = -1;

  private static final int BASE_TYPE = 0;

  private static final int AND_TYPE = 1;

  private static final int OR_TYPE = 2;

  private static final int NEST_TYPE = 3;

  private static final int FALSE_TYPE = 4;

  private final String originalLDAP;

  private final Node topNode;

  /* package */SimpleLdapMatcher(String paramOriginalLDAP) {
    originalLDAP = paramOriginalLDAP;

    if (originalLDAP != null) {
      ActiveString workString = new ActiveString(paramOriginalLDAP);
      topNode = parse(workString);
      workString.finish();
    } else {
      topNode = new Node();
      topNode.setType(FALSE_TYPE);
    }
  }

  /* package */SimpleLdapMatcher(Node node) {
    originalLDAP = null;
    topNode = node;
  }

  public static SimpleLdapMatcher create(String ldapFilter) {
    return new SimpleLdapMatcher(ldapFilter);
  }

  public static SimpleLdapMatcher create(Map<String, Object> props) {
    Node node = new Node();
    for (Entry<String, Object> entry : props.entrySet()) {
      Node child = new Node(entry.getKey(), true, stringOf(entry.getValue()));
      node.addChild(child);
    }
    return new SimpleLdapMatcher(node);
  }

  public static SimpleLdapMatcher createClassFilter(String clazzName) {
    Map<String, Object> props = new HashMap<String, Object>();
    props.put(Constants.OBJECTCLASS, clazzName);
    return create(props);

  }

  private static String stringOf(Object obj) {
    return (null == obj) ? null : obj.toString();
  }

  /* package */String getOriginalFilterString() {
    return originalLDAP;
  }

  /**
   * An or-type match checker
   * @param properties
   * @return
   */
  public boolean matches(MultiMap<String, String> properties) {
    if (properties == null) {
      throw new IllegalArgumentException();
    }
    Map<String, Set<String>> matchingMap = getMatchingMap(properties);
    return matches(matchingMap);
  }

  public boolean matches(Map<String, Set<String>> toMatch) {
    return topNode.doesMatch(toMatch);
  }
  
  public Set<String> getTheAndSetFor(String propName, boolean removeIt) {
    Set<String> andSet = new HashSet<String>();
    getTheAndSetFor(andSet, propName.toLowerCase(), removeIt, topNode);
    return andSet;
  }
  
  protected boolean getTheAndSetFor(Set<String> result, String propName, boolean removeIt, Node node) {
    boolean hadMatch = false;
    if (node.type == BASE_TYPE) {
      if (node.leftHandSide.equals(propName)) {
        result.add(node.rightHandSide);
        hadMatch = true;
        if (removeIt) {
          node.leftHandSide="";
        }
      }
    } else if (node.type == AND_TYPE) {
      Iterator<Node> iter = node.children.iterator();
      while (iter.hasNext()) {
        Node andNode = iter.next();
        boolean matched = getTheAndSetFor(result, propName, removeIt, andNode);
        hadMatch |= matched;
        if (matched && removeIt) {
          iter.remove();
        }
      }
    }
    return hadMatch;
  }


  private Map<String, Set<String>> getMatchingMap(MultiMap<String, String> props) {
    Map<String, Set<String>> result = new HashMap<String, Set<String>>();
    for (Entry<String, List<String>> entry : props.entrySet()) {
      String key = entry.getKey().toLowerCase();
      Set<String> set = result.get(key);
      if (null == set) {
        set = new HashSet<String>();
        result.put(entry.getKey().toLowerCase(), set);
      }
      set.addAll(entry.getValue());
    }
    return result;
  }

  /**
   * Static method that helps find a match
   * 
   * @param filter
   *          The filter to match
   * @param properties
   *          The properties to match them against
   * @return true if there is a match, false otherwise
   */
  /* package */static boolean filterMatch(String filter,
      Map<String, Object> properties) {
    if (filter == null || properties == null) {
      throw new IllegalArgumentException();
    }

    ActiveString workString = new ActiveString(filter);
    Node topNode = parse(workString);

    Map<String, Set<String>> matchingMap = getMatchingMap(properties);
    return topNode.doesMatch(matchingMap);
  }

  /* package */static Map<String, Set<String>> getMatchingMap(
      Map<String, Object> paramProperties) {
    HashMap<String, Set<String>> retVal = new HashMap<String, Set<String>>();

    // Get the "fixed" map AFTER adding the system generated properties
    for (Map.Entry<String, Object> entry : paramProperties.entrySet()) {
      String key = entry.getKey();
      if (key == null) {
        throw new RuntimeException("Null key in property map");
      }

      Object value = entry.getValue();
      if (value == null) {
        throw new RuntimeException("Null value in property map for " + key);
      }

      String toLowerKey = key.toLowerCase();
      Set<String> valueSet = new HashSet<String>();

      Class<?> valueClass = value.getClass();
      if (valueClass.isArray()) {
        int valueLength = Array.getLength(value);
        for (int lcv = 0; lcv < valueLength; lcv++) {
          Object arrayValue = Array.get(value, lcv);
          if (arrayValue == null) {
            throw new RuntimeException("Null value in array for " + key
                + " at index " + lcv);
          }

          String arrayValueString = arrayValue.toString();
          valueSet.add(arrayValueString);
        }
      } else {
        String valueString = value.toString();
        valueSet.add(valueString);
      }

      retVal.put(toLowerKey, valueSet);
    }

    return retVal;
  }

  private static Node parse(ActiveString workString) {
    Node retVal = new Node();

    // I am looking for the start of an expression
    workString.toStatementStart();

    int type = workString.getStatementType();
    switch (type) {
    case BASE_TYPE:
      retVal.setType(BASE_TYPE);
      String lhs = workString.getLeftHandSide();
      boolean isEquals = workString.getEqualsOrNotEquals();
      String rhs = workString.getRightHandSide();

      retVal.setLeftHandSide(lhs);
      retVal.setIsEquals(isEquals);
      retVal.setRightHandSide(rhs);
      break;
    case AND_TYPE:
    case OR_TYPE:
      retVal.setType(type);
      while (workString.finishedOrNewStatement()) {
        Node child = parse(workString);
        retVal.addChild(child);
      }

      if (retVal.getNumChildren() <= 1) {
        throw new RuntimeException(
            "There was zero or one children of an AND or OR construct");
      }

      break;
    case NEST_TYPE:
      // This type was NOT incremented past the '('
      Node lastNode = parse(workString);
      retVal.copyNode(lastNode);
      break;
    case FALSE_TYPE: // getStatementType cannot return FALSE_TYPE
    default:
      throw new AssertionError("Unknown node type " + type);
    }

    return retVal;
  }


  private static class Node {

    private int type = UNKNOWN_TYPE;

    // For base type
    private String leftHandSide;

    private boolean equals;

    private String rightHandSide;

    // For and and or types
    private final List<Node> children = new LinkedList<Node>();

    private Node() {
      this.type = AND_TYPE;
    }

    private Node(String lhs, boolean equals, String rhs) {
      this.type = BASE_TYPE;
      this.leftHandSide = lhs;
      this.equals = equals;
      this.rightHandSide = rhs;
    }

    private void setType(int paramType) {
      type = paramType;
    }

    private void setLeftHandSide(String lhs) {
      leftHandSide = lhs.toLowerCase();
    }

    private void setRightHandSide(String rhs) {
      rightHandSide = rhs;
    }

    private void setIsEquals(boolean isEquals) {
      equals = isEquals;
    }

    private void addChild(Node child) {
      children.add(child);
    }

    private int getNumChildren() {
      return children.size();
    }

    private void copyNode(Node copyNode) {
      type = copyNode.type;

      leftHandSide = copyNode.leftHandSide;
      equals = copyNode.equals;
      rightHandSide = copyNode.rightHandSide;

      children.clear();
      children.addAll(copyNode.children);
    }

    private boolean doesMatch(Map<String, Set<String>> toMatch) {
      if (type == BASE_TYPE) {
        Set<String> valueSet = toMatch.get(leftHandSide);
        if (valueSet == null) {
          if (equals) {
            return (null == leftHandSide || leftHandSide.length() == 0);
          }

          return true;
        }

        WildcardMatcher matcher = new WildcardMatcher(rightHandSide);
        for (String val : valueSet) {
          if (matcher.matches(val)) {
            return equals;
          }
        }

        // valueSet does *not* contain the value
        if (equals) {
          return false;
        }

        return true;
      }

      if (type == AND_TYPE) {
        for (Node andNode : children) {
          if (!andNode.doesMatch(toMatch)) {
            return false;
          }
        }

        return true;
      }

      if (type == OR_TYPE) {
        for (Node orNode : children) {
          if (orNode.doesMatch(toMatch)) {
            return true;
          }
        }

        return false;
      }

      if (type == FALSE_TYPE) {
        return false;
      }

      throw new AssertionError("Unknown node type " + type);
    }

    @Override
    public String toString() {
      String typeString;

      switch (type) {
      case BASE_TYPE:
        typeString = "BASE_TYPE";
        break;
      case AND_TYPE:
        typeString = "AND_TYPE";
        break;
      case OR_TYPE:
        typeString = "OR_TYPE";
        break;
      case FALSE_TYPE:
        typeString = "FALSE_TYPE";
        break;
      default:
        typeString = "Unknown_TYPE(" + type + ")";
      }

      return "Node(" + typeString + "," + "lhs=" + leftHandSide + ","
          + "equals=" + equals + "," + "rhs=" + rightHandSide + ","
          + System.identityHashCode(this) + ")";
    }
  }

  private static class ActiveString {
    final String original;

    int dot = 0;

    char data[];

    private ActiveString(String fullString) {
      original = fullString;
      data = fullString.toCharArray();
    }

    /**
     * Increments dot safely
     * 
     * @param doThrow
     *          Tells this method to throw if it reaches the end of the string,
     *          the state machine knows that this should not reach the end
     * @return true if there are more characters, false if this is the end
     */
    private boolean incrementDot(boolean doThrow) {
      if (dot + 1 >= data.length) {
        // finished
        if (doThrow) {
          throw new RuntimeException("Invalid end of string reached in "
              + original + " at character " + dot);
        }

        return false;
      }

      dot++;
      return true;
    }

    /**
     * Skips white space
     * 
     * @param doThrow
     *          if true throw an exception if the end is reached
     * @return true if there are more characters, false if this is the end
     */
    private boolean skipWhiteSpace(boolean doThrow) {
      boolean retVal = false;

      while (Character.isWhitespace(data[dot])) {
        retVal = incrementDot(doThrow);
      }

      return retVal;
    }

    private void toStatementStart() {
      skipWhiteSpace(true);

      if (!(data[dot] == LEFT)) {
        throw new RuntimeException("Statement did not start with '(', found "
            + data[dot] + " at character " + dot);
      }

      // Get past the left parenthesis
      incrementDot(true);
    }

    private int getStatementType() {
      skipWhiteSpace(true);

      if (data[dot] == AND) {
        incrementDot(true);
        return AND_TYPE;
      }

      if (data[dot] == OR) {
        incrementDot(true);
        return OR_TYPE;
      }

      if (data[dot] == LEFT) {
        // Do not increment dot
        return NEST_TYPE;
      }

      // Do not increment dot
      return BASE_TYPE;
    }

    /**
     * 
     * @return false if this is finished (increments past the final ')' and true
     *         if there is another statement (does NOT increment past the '(')
     */
    private boolean finishedOrNewStatement() {
      skipWhiteSpace(true);

      if (data[dot] == RIGHT) {
        incrementDot(false);
        return false;
      }

      // Do not increment past the dot
      return true;
    }

    private String getLeftHandSide() {
      int startIndex = dot;
      int count = 0;

      while (data[dot] != EQ) {
        if ((data[dot] == NOT) && ((dot + 1) < data.length)
            && (data[dot + 1] == EQ)) {
          // Found a not equals
          return new String(data, startIndex, count);
        }

        count++;
        incrementDot(true);
      }

      return new String(data, startIndex, count);
    }

    private boolean getEqualsOrNotEquals() {
      if (data[dot] == EQ) {
        incrementDot(true);
        return true;
      }

      if (data[dot] == NOT) {
        incrementDot(true);
        if (!(data[dot] == EQ)) {
          throw new AssertionError("Invalid not equals at character " + dot);
        }

        incrementDot(true);
        return false;
      }

      throw new AssertionError(
          "Was looking for equals or not equals but found " + data[dot]
              + " at character " + dot);
    }

    private String getRightHandSide() {
      int startIndex = dot;
      int count = 0;

      while (data[dot] != RIGHT) {
        count++;
        incrementDot(true);
      }

      // This might be the end, but need to get past the )
      incrementDot(false);
      return new String(data, startIndex, count);
    }

    private void finish() {
      if (skipWhiteSpace(false) == true) {
        throw new RuntimeException(
            "Found extra characters at end of LDAP string " + original
                + " at character " + dot);
      }
    }

    public String toString() {
      char dataDot = (dot < data.length) ? data[dot] : ' ';

      return "ActiveString(" + original + "," + dot + "," + dataDot + ")";
    }
  }

  public String toString() {
    return "Filter(" + originalLDAP + "," + System.identityHashCode(this) + ")";
  }

  public String getLdapExpression() {
    return originalLDAP;
  }
  
}
