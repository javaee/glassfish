/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000,2002,2003 INRIA, France Telecom 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package oracle.toplink.libraries.asm.tree.analysis;

import oracle.toplink.libraries.asm.Type;

/**
 * An extended {@link BasicVerifier} that performs more precise verifications.
 * This verifier computes exact class types, instead of using a single 
 * "object reference" type (as done in the {@link BasicVerifier}).  
 * 
 * @author Eric Bruneton
 * @author Bing Ran
 */

public class SimpleVerifier extends BasicVerifier {

  public Value newValue (final Type type) {
    Value v = super.newValue(type);
    if (v == BasicValue.REFERENCE_VALUE) {
      if (type.getSort() == Type.ARRAY) {
        v = newValue(type.getElementType());
        String desc = ((BasicValue)v).getType().getDescriptor();
        for (int i = 0; i < type.getDimensions(); ++i) {
          desc = "[" + desc;
        }
        v = new BasicValue(Type.getType(desc));
      } else {
        v = new BasicValue(type);
      }
    }
    return v;
  }
  
  protected boolean isArrayValue (final Value value) {
    Type t = ((BasicValue)value).getType();
    if (t != null) {
      return t.getDescriptor().equals("Lnull;") || t.getSort() == Type.ARRAY;
    } 
    return false;
  }
      
  protected Value getElementValue (final Value objectArrayValue)
    throws AnalyzerException
  {
    Type arrayType = ((BasicValue)objectArrayValue).getType();
    if (arrayType != null) {
      if (arrayType.getSort() == Type.ARRAY) {
        return newValue(Type.getType(arrayType.getDescriptor().substring(1)));
      } else if (arrayType.getDescriptor().equals("Lnull;")) {
        return objectArrayValue;
      }
    }
    throw new AnalyzerException("Not an array type");
  }
    
  protected boolean isSubTypeOf (final Value value, final Value expected) {
    Type expectedType = ((BasicValue)expected).getType();
    Type type = ((BasicValue)value).getType();
    if (expectedType == null) {
      return type == null;
    } else {
      switch (expectedType.getSort()) {
        case Type.INT:
        case Type.FLOAT:
        case Type.LONG:
        case Type.DOUBLE:
          return type == expectedType;
        case Type.ARRAY:
        case Type.OBJECT:
          if (expectedType.getDescriptor().equals("Lnull;")) {
            return type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY;
          }
          Class expectedClass = getClass(expectedType);
          if (type.getDescriptor().equals("Lnull;")) {
            return !expectedClass.isPrimitive();
          } else if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
            Class actualClass = getClass(type);
            return expectedClass.isAssignableFrom(actualClass);
          } else {
            return false;
          }
        default:
          throw new RuntimeException("Internal error");
      }
    }
  }

  public Value merge (final Value v, final Value w) {
    if (!v.equals(w)) {
      Type t = ((BasicValue)v).getType();
      Type u = ((BasicValue)w).getType();
      if (t != null && (t.getSort() == Type.OBJECT || t.getSort() == Type.ARRAY)) {
        if (u != null && (u.getSort() == Type.OBJECT || u.getSort() == Type.ARRAY)) {
          if (t.getDescriptor().equals("Lnull;")) {
            return w;
          }
          if (u.getDescriptor().equals("Lnull;")) {
            return v;
          }
          Class c = getClass(t);
          Class d = getClass(u);
          if (c.isAssignableFrom(d)) {
            return v;
          }
          if (d.isAssignableFrom(c)) {
            return w;
          }
          // TODO case of array classes of the same dimension
          // TODO should we look also for a common super interface?
          //      problem: there may be several possible common super interfaces
          do {
            if (c == null || c.isInterface()) {
              return BasicValue.REFERENCE_VALUE;
            } else {
              c = c.getSuperclass();
            }
            if (c.isAssignableFrom(d)) {
              return newValue(Type.getType(c));
            }
          } while (true);
        }
      }
      return BasicValue.UNINITIALIZED_VALUE;
    }
    return v;
  }
  
  protected Class getClass (final Type t) {
    try {
      if (t.getSort() == Type.ARRAY) {
        return Class.forName(t.getDescriptor().replace('/', '.'));
      } else {
        return Class.forName(t.getClassName());
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e.toString());
    }
  }
}
