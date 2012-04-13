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

import java.lang.ref.WeakReference;
import java.util.HashMap;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;

import spiralcraft.lang.spi.SimpleChannel;

public class LiteralNode<X>
  extends Node
{

  public static final LiteralNode<Boolean> FALSE
    =new LiteralNode<Boolean>(Boolean.FALSE,Boolean.class);
  public static final LiteralNode<Boolean> TRUE
    =new LiteralNode<Boolean>(Boolean.TRUE,Boolean.class);
  public static final LiteralNode<Void> NULL
    =new LiteralNode<Void>(null,Void.class);
  
  @SuppressWarnings("rawtypes")
  public static final HashMap<Object,WeakReference<LiteralNode>> refs
    =new HashMap<Object,WeakReference<LiteralNode>>();
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static final <X> LiteralNode<X> get(X object)
  {
 
    synchronized (refs)
    {
      LiteralNode<X> node;
      WeakReference<LiteralNode> nodeRef=refs.get(object);
      if (nodeRef!=null)
      { 
        node=(LiteralNode<X>) nodeRef.get();
        if (node!=null)
        { return node;
        }
        else
        { 
          node=new LiteralNode<X>(object);
          refs.put(object,new WeakReference<LiteralNode>(node));
        }
      
      }
      else
      {
        node=new LiteralNode<X>(object);
        refs.put(object,new WeakReference<LiteralNode>(node));
      }
      
      return node;
    }
    
  }
  
  private final SimpleChannel<X> _optic;

  @SuppressWarnings("unchecked") // Type check
  public LiteralNode(X value)
  { 
    _optic=new SimpleChannel<X>((Class<X>) value.getClass(),value,true);
    this.hashCode=calcHashCode();
  }

  public LiteralNode(X value,Class<X> valueClass)
  { 
    _optic=new SimpleChannel<X>(valueClass,value,true);
    this.hashCode=calcHashCode();
  }
  
  LiteralNode(SimpleChannel<X> _optic)
  { 
    this._optic=_optic; 
    this.hashCode=calcHashCode();
  }

  
  @Override
  public Node[] getSources()
  { return null;
  }
  
  @Override
  public Node copy(Object visitor)
  { return this;
  }
  
  @Override
  public String reconstruct()
  { 
    X val=_optic.get();
    if (val!=null)
    { 
      if (val instanceof String)
      { return "\""+val.toString()+"\"";
      }
      else if (val instanceof Character)
      { return "'"+val.toString()+"'";
      }
      else if (val instanceof Expression)
      { return "`"+val.toString()+"`";
      }
      else
      { return val.toString();
      }
    }
    else
    { return "null";
    }
  }
  
  @Override
  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix)
      .append("Literal: ").append(_optic.getContentType().getName())
      .append(":[")
      .append(_optic.getContentType()==String.class?"\"":"")
      .append(_optic.getContentType()==Expression.class?"`":"")
      .append(_optic.getContentType()==Character.class?"'":"")
      .append(_optic.get()!=null?_optic.get().toString():null)
      .append(_optic.getContentType()==Character.class?"'":"")
      .append(_optic.getContentType()==Expression.class?"`":"")
      .append(_optic.getContentType()==String.class?"\"":"")
      .append("]")
      ;
  }

  @Override
  public synchronized Channel<X> bind(final Focus<?> focus)
    throws BindException
  { 
//    System.out.println("LiteralNode: Returning "+_optic.toString());
    return _optic;
  }

  private int calcHashCode()
  {
    return (_optic.getContentType().hashCode()*31)
      +(_optic.get()!=null?_optic.get().hashCode():0);
  }
  
  @Override
  protected boolean equalsNode(Node node)
  {
    @SuppressWarnings("unchecked")
    LiteralNode<X> ln=(LiteralNode<X>) node;
    if (_optic.getContentType()!=ln._optic.getContentType())
    { return false;
    }
    X val=_optic.get();
    X other=ln._optic.get();
    if (val==other)
    { return true;
    }
    else if (val==null)
    { return false;
    }
    else 
    { return (val.equals(other));
    }
  }
 
  
  
}
