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

import java.util.Iterator;

import spiralcraft.lang.spi.BeanReflector;
import spiralcraft.lang.spi.Binding;
import spiralcraft.lang.spi.NamespaceReflector;

import spiralcraft.lang.Decorator;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Expression;

import spiralcraft.data.DataException;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;

import spiralcraft.data.lang.TupleBinding;
import spiralcraft.data.lang.DataReflector;

import spiralcraft.data.query.Queryable;

import spiralcraft.data.spi.CursorIterator;

import spiralcraft.data.access.SerialCursor;

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
public abstract class View<Ttuple extends Tuple>
{

  protected final NamespaceReflector namespaceReflector
    =new NamespaceReflector();
  
  protected ViewReflector<?,?> viewReflector;
  
  private String name;
  private TupleBinding<?> tupleBinding;
  private DataSession dataSession;
  protected Queryable<Tuple> queryable;
  protected Type<?> type;
  
  
  protected View()
  {
  }
  
  /**
   * Create a new SerialCursor which iterates through
   *   the visible Tuples in this view 
   */
  public abstract SerialCursor<Tuple> scan()
    throws DataException;
  
  void setDataSession(DataSession dataSession)
  { this.dataSession=dataSession;
  }
  
  /**
   * @return The DataSession that owns this View
   * 
   * @return
   */
  public DataSession getDataSession()
  { return dataSession;
  }
  
  public String getName()
  { return name;
  }
  
  public void setName(String name)
  { 
    // XXX Assert stopped
    this.name=name;
  }
  
  public TupleBinding<?> getTupleBinding()
  { return tupleBinding;
  }
  
  protected void setTupleBinding(TupleBinding<?> binding)
  { this.tupleBinding=binding;
  }
  
  public Type<?> getType()
  { return type;
  }
  
  
  /**
   * The View should bind to all data sources here
   */
  public abstract void bindData(Focus<?> focus)
    throws DataException;  

  @SuppressWarnings("unchecked") // Runtime getClass()
  Reflector<View> getViewReflector()
    throws DataException
  { 
    try
    { return new ViewReflector(getClass(),type);
    }
    catch (BindException x)
    { 
      throw new DataException
        ("View '"+name+"': Error reflecting Type "+type.getURI()
         +": "+x.toString()
        ,x
        ); 
            
    }
  }
  
  /** 
   * Return the Reflector associated with this view. 
   */
  Reflector<Tuple> getDataReflector()
    throws DataException
  {
    
    try
    { return DataReflector.getInstance(getType());
    }
    catch (BindException x)
    { 
      throw new DataException
        ("View '"+name+"': Error reflecting Type "+type.getURI()
         +": "+x.toString()
        ,x
        ); 
            
    }
  }

}


/**
 * Provides access to a View's Bean interface, with the added function of
 *   supporting Type aware iteration bindings.
 * 
 * @author mike
 *
 * @param <Tview>
 * @param <Ttuple>
 */
class ViewReflector<Tview extends View<Ttuple>,Ttuple extends Tuple>
  extends BeanReflector<Tview>
{ 
  
  //private Type type;
  private final Reflector<Tuple> dataReflector;
  

  public ViewReflector(Class<Tview> viewClass,Type<?> type)
    throws BindException
  { 
    super(viewClass);
    //this.type=type;
    this.dataReflector=DataReflector.<Tuple>getInstance(type);
  }
  
  @Override
  public Binding<Object> resolve
    (Binding<Tview> source
    ,Focus<?> focus
    ,String name
    ,Expression<?>[] params
    )
    throws BindException
  { return super.resolve(source,focus,name,params);
  }
  
  
  @Override
  @SuppressWarnings("unchecked") // Dynamic class info
  public  Decorator decorate
    (Binding source,Class decoratorInterface)
    throws BindException
  { 
    if (decoratorInterface==IterationDecorator.class)
    { 
      return new ViewIterationDecorator<Tview,Ttuple>
        (source
        ,dataReflector
        );
    }
    return null;
  }


}

/**
 * Iterates through the visible contents of a View as provided by
 *   the SerialCursor returned from View.scan()
 * 
 * @author mike
 *
 * @param <Tview>
 * @param <Ttuple>
 */
class ViewIterationDecorator<Tview extends View<Ttuple>,Ttuple extends Tuple>
  extends IterationDecorator<Tview,Tuple>
{

  public ViewIterationDecorator
    (Binding<Tview> viewSource
    ,Reflector<Tuple> iterationType
    )
  { super(viewSource,iterationType);
  }
  
  @Override
  protected Iterator<Tuple> createIterator()
  { 
    try
    { return new CursorIterator<Tuple>(source.get().scan());
    }
    catch (DataException x)
    { 
      throw new RuntimeDataException
        ("Error getting Cursor for iteration: "+x,x);
    }
    
  }
}
