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

  private String name;
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
    
  public Type<?> getType()
  { return type;
  }
  

  public State newState()
  { return new State();
  }
  
  public class State
  {
    
  }
  
}
