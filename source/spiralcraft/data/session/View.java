//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.session;

import spiralcraft.lang.spi.BeanReflector;
import spiralcraft.lang.spi.Binding;
import spiralcraft.lang.spi.Namespace;
import spiralcraft.lang.spi.NamespaceReflector;
import spiralcraft.lang.spi.SimpleBinding;

import spiralcraft.lang.Decorator;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;



import spiralcraft.data.DataException;
import spiralcraft.data.Type;

import spiralcraft.data.lang.TupleBinding;
import spiralcraft.data.lang.DataReflector;

import spiralcraft.data.query.Queryable;

/**
 * <P>A named component of a DataSession that provides access to a set of  
 *  Tuples of a common Type. 
 *
 * <P>A View provides a namespace for the spiralcraft.lang EL. All views expose
 *   the following namespace:
 *   
 * <UL>
 *   <LI><CODE>view</CODE>: A reference to the View implementation</LI>
 *   <LI><CODE>data</CODE>: A reference to the data field values at the cursor
 *                           position</LI>
 * </UL>
 * 
 */
public abstract class View
{

  protected final NamespaceReflector namespaceReflector
    =new NamespaceReflector();
  
  protected ViewReflector viewReflector;
  
  private Namespace namespace;
  
  private String name;
  private TupleBinding tupleBinding;
  private DataSession dataSession;
  protected Queryable queryable;
  protected Type type;
  
  
  protected View()
  {
  }
  
  void setDataSession(DataSession dataSession)
  { this.dataSession=dataSession;
  }
  
  public DataSession getDataSession()
  { return dataSession;
  }
  
  public String getName()
  { return name;
  }
  
  public TupleBinding getTupleBinding()
  { return tupleBinding;
  }
  
  public Type getType()
  { return type;
  }
  
  
  
  /**
   * The View should bind to all sources here
   */
  protected abstract TupleBinding bind(Focus focus)
    throws DataException;
  
  /** 
   * Return the Reflector associated with this view. 
   */
  Reflector getViewReflector()
    throws DataException
  {
    try
    {
      namespaceReflector.register
        ("view"
        ,BeanReflector.getInstance(getClass())
        );
      namespaceReflector.register
        ("data"
        ,DataReflector.getInstance(type)
        );
    }
    catch (BindException x)
    { 
      throw new DataException
        ("View '"+name+"': Internal error instantiating "+x.toString(),x);
    }
    return namespaceReflector;
  }
  
  Channel bindView(Focus focus)
    throws DataException
  {
    try
    {
      namespace=new Namespace(namespaceReflector);
      namespace.putOptic("view",new SimpleBinding<View>(this,true));

      TupleBinding tupleBinding=bind(focus);
      namespace.putOptic("data",tupleBinding);
      
    }
    catch (BindException x)
    { 
      throw new DataException
        ("View '"+name+"': Internal binding View to namespace "+x.toString(),x);
    }
    return new SimpleBinding<Namespace>(namespaceReflector,namespace,true);
    
  }
  

}

class ViewReflector<T extends View>
  extends BeanReflector<T>
{ 
  public ViewReflector(Class<T> viewClass)
  { super(viewClass);
  }
  
  @SuppressWarnings("unchecked") // Dynamic class info
  public <D extends Decorator<T>> D decorate
    (Binding<? extends T> source,Class<D> decoratorInterface)
    throws BindException
  { 
    if (decoratorInterface==IterationDecorator.class)
    { 

    }
    
    // Look up the target class in the map of decorators for 
    //   the specified interface?
    return null;
  }
}
