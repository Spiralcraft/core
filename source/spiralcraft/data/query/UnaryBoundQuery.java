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
package spiralcraft.data.query;

import java.util.List;

import spiralcraft.lang.Focus;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;

import spiralcraft.data.transport.SerialCursor;
import spiralcraft.data.transport.ScrollableCursor;

/**
 * <P>A BoundQuery that performs a transformation on the results
 *   of single other BoundQuery. One or more Tuples from the SourceQuery
 *   will generate an output Tuple (ie. they may be filtered, grouped or
 *   otherwise reduced). 
 *   
 * <P>This class helps to build Query operations which act on the output of
 *   another Query.
 *
 */
public abstract class UnaryBoundQuery<Tq extends Query>
  extends BoundQuery<Tq>
{

  // XXX It would be cleaner if some of these positional vars resided in the
  //  Cursor. BoundQuery should move to being threadsafe. If params are
  //  threadSafe or sharable, this is doable. But not everything can be
  //  threadSafe- ie. SQL queries have issues. Is that why we're going for more
  //  and shorter lived Binding instances? ThreadLocal for processing state
  //  might be our answer. Dovetails with execution context awareness, b/c need
  //  a container component stacking the context into the Thread.
  
  protected final BoundQuery<?> source;
  private boolean resolved;
  protected boolean eos;
  protected boolean bos;
  
  protected int direction;
  protected int lookahead;
  
  protected UnaryBoundQuery(List<Query> sources,Focus focus,Queryable store)
    throws DataException
  { 
    if (sources.size()<1)
    { throw new DataException(getClass().getName()+": No source to bind to");
    }
    
    if (sources.size()<1)
    { throw new DataException(getClass().getName()+": Can't bind to more than one source");
    }

    BoundQuery source=store.query(sources.get(0),focus);

    this.source=source;
  }
  
  /**
   * Integrate the specified sourceTuple into the next result Tuple to be computed,
   *   as the result Cursor moves.
   * 
   * If the specified Tuple is null, this signifies that the end or beginning of
   *   the stream has been reached. 
   *   
   * If the specified Tuple is -not- part of the current result Tuple, the method
   *   lookedAhead() should be called and the sourceTuple will be passed to
   *   the integrate method again when Cursor movement resumes.
   * 
   * @return true when a new result Tuple is available, or false if
   *   no result Tuple is available.
   */
  protected abstract boolean integrate(Tuple sourceTuple);
  
  protected final void lookedAhead()
  { lookahead+=direction;
  }
  
  public SerialCursor execute()
    throws DataException
  {
    if (!resolved)
    { resolve();
    }
    bos=true;
    eos=false;
    integrate(null);
    
    direction=1;
    lookahead=0;
    
    SerialCursor cursor=source.execute();
    if (cursor instanceof ScrollableCursor)
    { return new UnaryBoundQueryScrollableCursor((ScrollableCursor) cursor);
    }
    else
    { return new UnaryBoundQuerySerialCursor(cursor);
    }
    
  }


  public void resolve()
    throws DataException
  { 
    super.resolve();
    if (!resolved)
    { 
      source.resolve();
      resolved=true;
    }
    else
    { throw new IllegalStateException("Already resolved");
    }
  }
  
  class UnaryBoundQuerySerialCursor
    extends BoundQuerySerialCursor
  {
    private final SerialCursor sourceCursor;
    
    public UnaryBoundQuerySerialCursor(SerialCursor sourceCursor)
    { this.sourceCursor=sourceCursor;
    }
    
    public boolean dataNext()
      throws DataException
    {
      direction=1;
      while (true)
      {
        if (lookahead<=0)
        {
          // Only advance the source if we've consumed
          //   data in the forward direction.
          if (!eos)
          { 
            if (!sourceCursor.dataNext())
            { 
              // End of stream
              // The end of the stream is always consumed
              eos=true;
              boolean ret=integrate(null);
              lookahead=0;
              return ret;
            }
            else
            { bos=false;
            }
          }
          else
          { 
            // We are at the end
            return false;
          }
        }
//        System.err.println
//          ("UnaryBoundQuery: dataNext() "+sourceCursor.dataGetTuple());
        lookahead=0;
        if (integrate(sourceCursor.dataGetTuple()))
        { return true;
        }
        else
        {
          // We know the lookahead has been used at this point.
          lookahead=0;
        }
        
      } 
    }
  }

  class UnaryBoundQueryScrollableCursor
    extends UnaryBoundQuerySerialCursor
    implements ScrollableCursor
  {
    private final ScrollableCursor sourceCursor;

    public UnaryBoundQueryScrollableCursor(ScrollableCursor sourceCursor)
    { 
      super(sourceCursor);
      this.sourceCursor=sourceCursor;
    }

    public void dataMoveAfterLast() throws DataException
    { while(dataNext());
    }

    public void dataMoveBeforeFirst() throws DataException
    { while(dataPrevious());
    }

    public boolean dataMoveFirst() throws DataException
    { 
      dataMoveBeforeFirst();
      return dataNext();
    }

    public boolean dataMoveLast() throws DataException
    {
      dataMoveAfterLast();
      return dataPrevious();
    }

    public boolean dataPrevious() throws DataException
    {
      direction=-1;
      while (true)
      {
        if (lookahead>=0)
        {
          // Only advance the source if we've consumed
          //   data in the reverse direction.
          if (!bos)
          { 
            if (!sourceCursor.dataPrevious())
            { 
              // End of stream
              // The end of the stream is always consumed
              bos=true;
              boolean ret=integrate(null);
              lookahead=0;
              return ret;
            }
            else
            { eos=false;
            }
          }
          else
          { 
            // We are at the beginning
            return false;
          }
        }
        
        lookahead=0;
        if (integrate(sourceCursor.dataGetTuple()))
        { return true;
        }
        else
        {
          // We know the lookahead has been used at this point.
          lookahead=0;
        }
      } 
    }
  }
}
