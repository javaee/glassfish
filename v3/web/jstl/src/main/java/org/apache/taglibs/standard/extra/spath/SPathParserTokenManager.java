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

public class SPathParserTokenManager implements SPathParserConstants
{
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private final int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 42:
         return jjStopAtPos(0, 14);
      case 47:
         return jjStopAtPos(0, 13);
      case 58:
         return jjStopAtPos(0, 15);
      case 61:
         return jjStopAtPos(0, 19);
      case 64:
         return jjStopAtPos(0, 18);
      case 91:
         return jjStopAtPos(0, 16);
      case 93:
         return jjStopAtPos(0, 17);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec3 = {
   0x0L, 0xffffffffffffc000L, 0xfffff0007fffffffL, 0x7fffffL
};
static final long[] jjbitVec4 = {
   0x0L, 0x0L, 0x0L, 0xff7fffffff7fffffL
};
static final long[] jjbitVec5 = {
   0x7ff3ffffffffffffL, 0x7ffffffffffffdfeL, 0xffffffffffffffffL, 0xfc31ffffffffe00fL
};
static final long[] jjbitVec6 = {
   0xffffffL, 0xffffffffffff0000L, 0xf80001ffffffffffL, 0x3L
};
static final long[] jjbitVec7 = {
   0x0L, 0x0L, 0xfffffffbffffd740L, 0xffffd547f7fffL
};
static final long[] jjbitVec8 = {
   0xffffffffffffdffeL, 0xffffffffdffeffffL, 0xffffffffffff0003L, 0x33fcfffffff199fL
};
static final long[] jjbitVec9 = {
   0xfffe000000000000L, 0xfffffffe027fffffL, 0x7fL, 0x707ffffff0000L
};
static final long[] jjbitVec10 = {
   0x7fffffe00000000L, 0xfffe0000000007feL, 0x7cffffffffffffffL, 0x60002f7fffL
};
static final long[] jjbitVec11 = {
   0x23ffffffffffffe0L, 0x3ff000000L, 0x3c5fdfffff99fe0L, 0x30003b0000000L
};
static final long[] jjbitVec12 = {
   0x36dfdfffff987e0L, 0x1c00005e000000L, 0x23edfdfffffbafe0L, 0x100000000L
};
static final long[] jjbitVec13 = {
   0x23cdfdfffff99fe0L, 0x3b0000000L, 0x3bfc718d63dc7e0L, 0x0L
};
static final long[] jjbitVec14 = {
   0x3effdfffffddfe0L, 0x300000000L, 0x3effdfffffddfe0L, 0x340000000L
};
static final long[] jjbitVec15 = {
   0x3fffdfffffddfe0L, 0x300000000L, 0x0L, 0x0L
};
static final long[] jjbitVec16 = {
   0xd7ffffffffffeL, 0x3fL, 0x200d6caefef02596L, 0x1fL
};
static final long[] jjbitVec17 = {
   0x0L, 0x3fffffffeffL, 0x0L, 0x0L
};
static final long[] jjbitVec18 = {
   0x0L, 0x0L, 0xffffffff00000000L, 0x7fffffffff003fL
};
static final long[] jjbitVec19 = {
   0x500000000007daedL, 0x2c62ab82315001L, 0xf580c90040000000L, 0x201080000000007L
};
static final long[] jjbitVec20 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffff0fffffffL, 0x3ffffffffffffffL
};
static final long[] jjbitVec21 = {
   0xffffffff3f3fffffL, 0x3fffffffaaff3f3fL, 0x5fdfffffffffffffL, 0x1fdc1fff0fcf1fdcL
};
static final long[] jjbitVec22 = {
   0x4c4000000000L, 0x0L, 0x7L, 0x0L
};
static final long[] jjbitVec23 = {
   0x3fe00000080L, 0xfffffffffffffffeL, 0xfffffffe001fffffL, 0x7ffffffffffffffL
};
static final long[] jjbitVec24 = {
   0x1fffffffffe0L, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec25 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0x3fffffffffL, 0x0L
};
static final long[] jjbitVec26 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0xfffffffffL, 0x0L
};
static final long[] jjbitVec27 = {
   0x0L, 0x0L, 0x80000000000000L, 0xff7fffffff7fffffL
};
static final long[] jjbitVec28 = {
   0xffffffL, 0xffffffffffff0000L, 0xf80001ffffffffffL, 0x30003L
};
static final long[] jjbitVec29 = {
   0xffffffffffffffffL, 0x30000003fL, 0xfffffffbffffd7c0L, 0xffffd547f7fffL
};
static final long[] jjbitVec30 = {
   0xffffffffffffdffeL, 0xffffffffdffeffffL, 0xffffffffffff007bL, 0x33fcfffffff199fL
};
static final long[] jjbitVec31 = {
   0xfffe000000000000L, 0xfffffffe027fffffL, 0xbbfffffbfffe007fL, 0x707ffffff0016L
};
static final long[] jjbitVec32 = {
   0x7fffffe00000000L, 0xffff03ff0007ffffL, 0x7cffffffffffffffL, 0x3ff3dffffef7fffL
};
static final long[] jjbitVec33 = {
   0xf3ffffffffffffeeL, 0xffcfff1e3fffL, 0xd3c5fdfffff99feeL, 0x3ffcfb080399fL
};
static final long[] jjbitVec34 = {
   0xd36dfdfffff987e4L, 0x1fffc05e003987L, 0xf3edfdfffffbafeeL, 0xffc100003bbfL
};
static final long[] jjbitVec35 = {
   0xf3cdfdfffff99feeL, 0xffc3b0c0398fL, 0xc3bfc718d63dc7ecL, 0xff8000803dc7L
};
static final long[] jjbitVec36 = {
   0xc3effdfffffddfeeL, 0xffc300603ddfL, 0xc3effdfffffddfecL, 0xffc340603ddfL
};
static final long[] jjbitVec37 = {
   0xc3fffdfffffddfecL, 0xffc300803dcfL, 0x0L, 0x0L
};
static final long[] jjbitVec38 = {
   0x7ff7ffffffffffeL, 0x3ff7fffL, 0x3bff6caefef02596L, 0x3ff3f5fL
};
static final long[] jjbitVec39 = {
   0xc2a003ff03000000L, 0xfffe03fffffffeffL, 0x2fe3ffffebf0fdfL, 0x0L
};
static final long[] jjbitVec40 = {
   0x0L, 0x0L, 0x0L, 0x21fff0000L
};
static final long[] jjbitVec41 = {
   0x3efffe000000a0L, 0xfffffffffffffffeL, 0xfffffffe661fffffL, 0x77ffffffffffffffL
};
private final int jjMoveNfa_0(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 19;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if (curChar == 39)
                     jjCheckNAddStates(0, 2);
                  else if (curChar == 34)
                     jjCheckNAddStates(3, 5);
                  break;
               case 1:
                  if ((0xfffffffbffffffffL & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 3:
                  if (curChar == 34)
                     jjCheckNAddStates(3, 5);
                  break;
               case 4:
                  if (curChar == 34 && kind > 1)
                     kind = 1;
                  break;
               case 5:
               case 8:
                  if (curChar == 39)
                     jjCheckNAddStates(0, 2);
                  break;
               case 6:
                  if ((0xffffff7fffffffffL & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 9:
                  if (curChar == 39 && kind > 1)
                     kind = 1;
                  break;
               case 11:
                  if ((0x3ff600000000000L & l) != 0L)
                     jjAddStates(6, 7);
                  break;
               case 12:
                  if (curChar == 58)
                     jjstateSet[jjnewStateCnt++] = 13;
                  break;
               case 14:
                  if ((0x3ff600000000000L & l) == 0L)
                     break;
                  if (kind > 2)
                     kind = 2;
                  jjstateSet[jjnewStateCnt++] = 14;
                  break;
               case 15:
                  if ((0x3ff600000000000L & l) == 0L)
                     break;
                  if (kind > 3)
                     kind = 3;
                  jjstateSet[jjnewStateCnt++] = 15;
                  break;
               case 16:
                  if ((0x3ff600000000000L & l) != 0L)
                     jjAddStates(8, 9);
                  break;
               case 17:
                  if (curChar == 58)
                     jjstateSet[jjnewStateCnt++] = 18;
                  break;
               case 18:
                  if (curChar == 42 && kind > 4)
                     kind = 4;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 2)
                     kind = 2;
                  jjCheckNAddStates(10, 15);
                  break;
               case 1:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(3, 5);
                  break;
               case 2:
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 3:
                  if (curChar == 92)
                     jjCheckNAddStates(3, 5);
                  break;
               case 6:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(0, 2);
                  break;
               case 7:
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 8;
                  break;
               case 8:
                  if (curChar == 92)
                     jjCheckNAddStates(0, 2);
                  break;
               case 11:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(11, 12);
                  break;
               case 13:
               case 14:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 2)
                     kind = 2;
                  jjCheckNAdd(14);
                  break;
               case 15:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 3)
                     kind = 3;
                  jjCheckNAdd(15);
                  break;
               case 16:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(16, 17);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 2)
                     kind = 2;
                  jjCheckNAddStates(10, 15);
                  break;
               case 1:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(3, 5);
                  break;
               case 6:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                     jjAddStates(0, 2);
                  break;
               case 11:
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(11, 12);
                  break;
               case 13:
                  if (!jjCanMove_1(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 2)
                     kind = 2;
                  jjCheckNAdd(14);
                  break;
               case 14:
                  if (!jjCanMove_2(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 2)
                     kind = 2;
                  jjCheckNAdd(14);
                  break;
               case 15:
                  if (!jjCanMove_2(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 3)
                     kind = 3;
                  jjCheckNAdd(15);
                  break;
               case 16:
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(16, 17);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 19 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   6, 7, 9, 1, 2, 4, 11, 12, 16, 17, 11, 12, 14, 15, 16, 17, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default : 
         if ((jjbitVec0[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_1(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec4[i2] & l2) != 0L);
      case 1:
         return ((jjbitVec5[i2] & l2) != 0L);
      case 2:
         return ((jjbitVec6[i2] & l2) != 0L);
      case 3:
         return ((jjbitVec7[i2] & l2) != 0L);
      case 4:
         return ((jjbitVec8[i2] & l2) != 0L);
      case 5:
         return ((jjbitVec9[i2] & l2) != 0L);
      case 6:
         return ((jjbitVec10[i2] & l2) != 0L);
      case 9:
         return ((jjbitVec11[i2] & l2) != 0L);
      case 10:
         return ((jjbitVec12[i2] & l2) != 0L);
      case 11:
         return ((jjbitVec13[i2] & l2) != 0L);
      case 12:
         return ((jjbitVec14[i2] & l2) != 0L);
      case 13:
         return ((jjbitVec15[i2] & l2) != 0L);
      case 14:
         return ((jjbitVec16[i2] & l2) != 0L);
      case 15:
         return ((jjbitVec17[i2] & l2) != 0L);
      case 16:
         return ((jjbitVec18[i2] & l2) != 0L);
      case 17:
         return ((jjbitVec19[i2] & l2) != 0L);
      case 30:
         return ((jjbitVec20[i2] & l2) != 0L);
      case 31:
         return ((jjbitVec21[i2] & l2) != 0L);
      case 33:
         return ((jjbitVec22[i2] & l2) != 0L);
      case 48:
         return ((jjbitVec23[i2] & l2) != 0L);
      case 49:
         return ((jjbitVec24[i2] & l2) != 0L);
      case 159:
         return ((jjbitVec25[i2] & l2) != 0L);
      case 215:
         return ((jjbitVec26[i2] & l2) != 0L);
      default : 
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_2(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec27[i2] & l2) != 0L);
      case 1:
         return ((jjbitVec5[i2] & l2) != 0L);
      case 2:
         return ((jjbitVec28[i2] & l2) != 0L);
      case 3:
         return ((jjbitVec29[i2] & l2) != 0L);
      case 4:
         return ((jjbitVec30[i2] & l2) != 0L);
      case 5:
         return ((jjbitVec31[i2] & l2) != 0L);
      case 6:
         return ((jjbitVec32[i2] & l2) != 0L);
      case 9:
         return ((jjbitVec33[i2] & l2) != 0L);
      case 10:
         return ((jjbitVec34[i2] & l2) != 0L);
      case 11:
         return ((jjbitVec35[i2] & l2) != 0L);
      case 12:
         return ((jjbitVec36[i2] & l2) != 0L);
      case 13:
         return ((jjbitVec37[i2] & l2) != 0L);
      case 14:
         return ((jjbitVec38[i2] & l2) != 0L);
      case 15:
         return ((jjbitVec39[i2] & l2) != 0L);
      case 16:
         return ((jjbitVec18[i2] & l2) != 0L);
      case 17:
         return ((jjbitVec19[i2] & l2) != 0L);
      case 30:
         return ((jjbitVec20[i2] & l2) != 0L);
      case 31:
         return ((jjbitVec21[i2] & l2) != 0L);
      case 32:
         return ((jjbitVec40[i2] & l2) != 0L);
      case 33:
         return ((jjbitVec22[i2] & l2) != 0L);
      case 48:
         return ((jjbitVec41[i2] & l2) != 0L);
      case 49:
         return ((jjbitVec24[i2] & l2) != 0L);
      case 159:
         return ((jjbitVec25[i2] & l2) != 0L);
      case 215:
         return ((jjbitVec26[i2] & l2) != 0L);
      default : 
         if ((jjbitVec3[i1] & l1) != 0L)
            return true;
         return false;
   }
}
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, null, null, null, null, null, 
"\57", "\52", "\72", "\133", "\135", "\100", "\75", };
public static final String[] lexStateNames = {
   "DEFAULT", 
};
private ASCII_UCodeESC_CharStream input_stream;
private final int[] jjrounds = new int[19];
private final int[] jjstateSet = new int[38];
protected char curChar;
public SPathParserTokenManager(ASCII_UCodeESC_CharStream stream)
{
   if (ASCII_UCodeESC_CharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public SPathParserTokenManager(ASCII_UCodeESC_CharStream stream, int lexState)
{
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(ASCII_UCodeESC_CharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 19; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(ASCII_UCodeESC_CharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

private final Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

public final Token getNextToken() 
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
         matchedToken = jjFillToken();
         return matchedToken;
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

}
