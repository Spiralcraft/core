//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.lang.parser;


import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
//import spiralcraft.log.ClassLogger;
import spiralcraft.lang.Reflector;
import spiralcraft.util.lang.ClassUtil;


public class EqualityNode<X>
  extends LogicalNode<X,X>
{
//  private static final ClassLogger log
//    =ClassLogger.getInstance(EqualityNode.class);

  private final boolean _negate;

  public EqualityNode(boolean negate,Node op1,Node op2)
  { 
    super(op1,op2);
    _negate=negate;
  }
  
  
  @Override
  public Node copy(Object visitor)
  { 
    EqualityNode<X> copy
      =new EqualityNode<X>(_negate,_op1.copy(visitor),_op2.copy(visitor));
    if (sameOperandNodes(copy))
    { return this;
    }
    else
    { return copy;
    }
  }
  
  @Override
  public String reconstruct()
  { return reconstruct(_negate?"!=":"==");
  }

  @Override
  protected LogicalTranslator 
    newTranslator(final Reflector<X> r1,final Reflector<X> r2)
      throws BindException
  { 
    
    return new RelationalTranslator(r1,r2)
    {  
      Class<X> c1=r1.getContentType();
      Class<X> c2=r2.getContentType();
      
      boolean number
        =Number.class.isAssignableFrom(c1.isPrimitive()?ClassUtil.boxedEquivalent(c1):c1)
        && Number.class.isAssignableFrom(c2.isPrimitive()?ClassUtil.boxedEquivalent(c2):c2);
      
      @Override
      protected void checkTypes()
        throws BindException
      { 
        if (coercion==null 
            && r1!=r2
            && _op2!=LiteralNode.NULL
            && !r1.isAssignableFrom(r2)
            && !r2.isAssignableFrom(r1)
            )
        { throw new BindException
            ("Incompatible types: "+r1.getTypeURI()
            +" cannot be compared to "+r2.getTypeURI()
            );
        }
      }
      
      @SuppressWarnings("unchecked")
      @Override
      public Boolean translateForGet(X val,Channel<?>[] mods)
      { 
        Object mod=mods[0].get();
        //    log.fine("EqualityNode: "+val+" == "+mod);
        if (val==mod)
        { return toResult(true);
        }
        else if (val==null || mod==null)
        { return toResult(false);
        }
        else if (number)
        {
          // Don't use .equals for numbers b/c of BigDecimal issues
          if (coercion!=null)
          { 
            try
            { 
//              log.fine("Using coercion to compare number "
//                +mod+" ("+mod.getClass()+") to "+val+"("+val.getClass()+")");
              return toResult
                  (((Comparable<X>) val)
                    .compareTo(coercion.coerce((X) mod))==0
                  );
            }
            catch (ClassCastException x)
            { 
              log.warning
                ( val+" ("+val.getClass()+")"
                  +" is not an "+r1.getContentType()
                );
              throw x;
            }
          }
          else
          { 
            return toResult(((Comparable<X>) val).compareTo((X) mod)==0);
          }
        }
        else if (val!=null && val.equals(mod))
        { return toResult(true);
        }
        return toResult(false);
      }
      
      private Boolean toResult(boolean value)
      { 
        return value
            ?(_negate?Boolean.FALSE:Boolean.TRUE)
            :(_negate?Boolean.TRUE:Boolean.FALSE);
      }
      
      /**
       * Equality is only a function if both arguments are immutable
       */
      @Override
      public boolean isFunction()
      { return r1.isImmutable() && r2.isImmutable();
      }
  
    };
  }
  
  public boolean isNegated()
  { return _negate;
  }
  
  @Override
  public String getSymbol()
  { return _negate?"!=":"==";
  }

}
