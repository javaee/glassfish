/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.taglibs.standard.extra.spath;

import java.util.Vector;

public class SPathParser implements SPathParserConstants {

  /** Simple command-line parser interface, primarily for testing. */
  public static void main(String args[]) throws ParseException
  {
    SPathParser parser = new SPathParser(System.in);
    Path p = parser.expression();
    java.util.List l = p.getSteps();

    // output for simple testing
    System.out.println();
    if (p instanceof AbsolutePath)
        System.out.println("Root: /");
    for (int i = 0; i < l.size(); i++) {
        Step s = (Step) l.get(i);
        System.out.print("Step: " + s.getName());
        if (s.isDepthUnlimited())
            System.out.print("(*)");
        System.out.println();
    }
  }

  // custom constructor to accept a String
  public SPathParser(String x) {
    this(new java.io.StringReader(x));
  }

//*********************************************************************
// Actual SPath grammar
  final public Path expression() throws ParseException {
  Path expr;
    if (jj_2_1(2147483647)) {
      expr = absolutePath();
      jj_consume_token(0);
    } else {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case QNAME:
      case NSWILDCARD:
      case SLASH:
      case STAR:
        expr = relativePath();
        jj_consume_token(0);
        break;
      default:
        jj_la1[0] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
   {if (true) return expr;}
    throw new Error("Missing return statement in function");
  }

  final public AbsolutePath absolutePath() throws ParseException {
  RelativePath relPath;
    jj_consume_token(SLASH);
    relPath = relativePath();
   {if (true) return new AbsolutePath(relPath);}
    throw new Error("Missing return statement in function");
  }

// as an example, we use recursion here to handle a list
  final public RelativePath relativePath() throws ParseException {
  RelativePath relPath = null;
  Step step;
    step = step();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case SLASH:
      jj_consume_token(SLASH);
      relPath = relativePath();
      break;
    default:
      jj_la1[1] = jj_gen;
      ;
    }
   {if (true) return new RelativePath(step, relPath);}
    throw new Error("Missing return statement in function");
  }

// as an example, we use inline code here to handle a list
/* 
 * (i'm doing something perhaps unusual here, including the <SLASH>
 * as if it were part of the step.  this mechanism for differentiating
 * '/' from '//' seems most natural, even if it is a bit unconventional.)
 */
  final public Step step() throws ParseException {
  Token slash = null;
  String nt;
  Vector pl = null;
  Predicate p;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case SLASH:
      slash = jj_consume_token(SLASH);
      break;
    default:
      jj_la1[2] = jj_gen;
      ;
    }
    nt = nameTest();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case START_BRACKET:
        ;
        break;
      default:
        jj_la1[3] = jj_gen;
        break label_1;
      }
      p = predicate();
          if (pl == null) pl = new Vector();
          pl.add(p);
    }
   // if 'slash != null', then we have '//' versus '/'
   {if (true) return new Step(slash != null, nt, pl);}
    throw new Error("Missing return statement in function");
  }

  final public String nameTest() throws ParseException {
  Token name;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case STAR:
      name = jj_consume_token(STAR);
      break;
    case NSWILDCARD:
      name = jj_consume_token(NSWILDCARD);
      break;
    case QNAME:
      name = jj_consume_token(QNAME);
      break;
    default:
      jj_la1[4] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
   {if (true) return name.toString();}
    throw new Error("Missing return statement in function");
  }

  final public Predicate predicate() throws ParseException {
  Predicate p;
    jj_consume_token(START_BRACKET);
    p = attributePredicate();
    jj_consume_token(END_BRACKET);
    {if (true) return p;}
    throw new Error("Missing return statement in function");
  }

  final public Predicate attributePredicate() throws ParseException {
  Token attname, target;
    jj_consume_token(AT);
    attname = jj_consume_token(QNAME);
    jj_consume_token(EQUALS);
    target = jj_consume_token(LITERAL);
    {if (true) return new AttributePredicate(attname.toString(), target.toString());}
    throw new Error("Missing return statement in function");
  }

  final private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    boolean retval = !jj_3_1();
    jj_save(0, xla);
    return retval;
  }

  final private boolean jj_3R_13() {
    if (jj_scan_token(AT)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(QNAME)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(EQUALS)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(LITERAL)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3_1() {
    if (jj_3R_2()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_10() {
    if (jj_scan_token(NSWILDCARD)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_11() {
    if (jj_scan_token(QNAME)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_2() {
    if (jj_scan_token(SLASH)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_3()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_12() {
    if (jj_scan_token(START_BRACKET)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_13()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_scan_token(END_BRACKET)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_8() {
    if (jj_3R_12()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_5() {
    if (jj_scan_token(SLASH)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_3()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_6() {
    if (jj_scan_token(SLASH)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_3() {
    if (jj_3R_4()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_5()) jj_scanpos = xsp;
    else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_4() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_6()) jj_scanpos = xsp;
    else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    if (jj_3R_7()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_8()) { jj_scanpos = xsp; break; }
      if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    }
    return false;
  }

  final private boolean jj_3R_9() {
    if (jj_scan_token(STAR)) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  final private boolean jj_3R_7() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_9()) {
    jj_scanpos = xsp;
    if (jj_3R_10()) {
    jj_scanpos = xsp;
    if (jj_3R_11()) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) return false;
    return false;
  }

  public SPathParserTokenManager token_source;
  ASCII_UCodeESC_CharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  public boolean lookingAhead = false;
  private boolean jj_semLA;
  private int jj_gen;
  final private int[] jj_la1 = new int[5];
  final private int[] jj_la1_0 = {0x6014,0x2000,0x2000,0x10000,0x4014,};
  final private JJCalls[] jj_2_rtns = new JJCalls[1];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  public SPathParser(java.io.InputStream stream) {
    jj_input_stream = new ASCII_UCodeESC_CharStream(stream, 1, 1);
    token_source = new SPathParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(java.io.InputStream stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public SPathParser(java.io.Reader stream) {
    jj_input_stream = new ASCII_UCodeESC_CharStream(stream, 1, 1);
    token_source = new SPathParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public SPathParser(SPathParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  public void ReInit(SPathParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  final private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    return (jj_scanpos.kind != kind);
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = lookingAhead ? jj_scanpos : token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      boolean exists = false;
      for (java.util.Enumeration enum_ = jj_expentries.elements(); enum_.hasMoreElements();) {
        int[] oldentry = (int[])(enum_.nextElement());
        if (oldentry.length == jj_expentry.length) {
          exists = true;
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              exists = false;
              break;
            }
          }
          if (exists) break;
        }
      }
      if (!exists) jj_expentries.addElement(jj_expentry);
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  final public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[20];
    for (int i = 0; i < 20; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 5; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 20; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

  final private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 1; i++) {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
          }
        }
        p = p.next;
      } while (p != null);
    }
    jj_rescan = false;
  }

  final private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
