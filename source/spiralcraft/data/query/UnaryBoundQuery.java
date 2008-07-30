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

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;

import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.lang.TupleReflector;

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
public abstract class UnaryBoundQuery
  <Tq extends Query,Tt extends Tuple,Ts extends Tuple>
  extends BoundQuery<Tq,Tt>
{

  protected final BoundQuery<?,? extends Tuple> source;
  private boolean resolved;
  protected ThreadLocalChannel<Ts> sourceChannel;
  
  protected UnaryBoundQuery(List<Query> sources,Focus<?> focus,Queryable<?> store)
    throws DataException
  { 
    if (sources.size()<1)
    { throw new DataException(getClass().getName()+": No source to bind to");
    }
    
    if (sources.size()>1)
    { throw new DataException(getClass().getName()+": Can't bind to more than one source");
    }
    

    BoundQuery<?,?> source=store.query(sources.get(0),focus);

    this.source=source;
    if (source==null)
    { 
      throw new DataException
        ("Querying "+store+" returned null (unsupported Type?) for "+sources.get(0).toString());
    }
  }
  

  
  protected abstract SerialCursor<Tt> 
    newSerialCursor(SerialCursor<Ts> source) throws DataException;

  protected abstract ScrollableCursor<Tt> 
    newScrollableCursor(ScrollableCursor<Ts> source) throws DataException;

  @SuppressWarnings("unchecked") // Converting from source Tuple type
  public SerialCursor<Tt> execute()
    throws DataException
  {
    if (!resolved)
    { resolve();
    }
    
    SerialCursor<Ts> cursor=(SerialCursor<Ts>) source.execute();
    SerialCursor<Tt> ret=null;
    if (cursor instanceof ScrollableCursor)
    { ret=newScrollableCursor((ScrollableCursor<Ts>) cursor);
    }
    if (ret==null)
    { ret=newSerialCursor(cursor);
    }
    if (debug)
    { log.fine(toString()+" execute returning "+ret);
    }
    return ret;
  }


  public void resolve()
    throws DataException
  { 
    super.resolve();
    if (!resolved)
    { 
      source.resolve();
      try
      {
        sourceChannel
          =new ThreadLocalChannel<Ts>
            (new TupleReflector<Ts>(source.getQuery().getFieldSet(),null));
      }
      catch (BindException x)
      { throw new DataException("Error resolving Query",x);
      }
      resolved=true;
    }
    else
    { log.fine("Query "+this+" already resolved");
    }
  }
  
  abstract class UnaryBoundQuerySerialCursor
    extends BoundQuerySerialCursor
  {
    protected final SerialCursor<Ts> sourceCursor;
    
    protected boolean eos;
    protected boolean bos;
    
    protected int direction;
    protected int lookahead;
    
    public UnaryBoundQuerySerialCursor(SerialCursor<Ts> sourceCursor)
      throws DataException
    { 
      bos=true;
      eos=false;
      
      integrate(null);
      
      direction=1;
      lookahead=0;

      this.sourceCursor=sourceCursor;
    }

    /**
     * Integrate the Tuple available from the sourceChannel into the next 
     *   result Tuple to be computed, as the result Cursor moves.
     * 
     * If the specified Tuple is null, this signifies that the end or beginning
     *   of the stream has been reached. 
     *   
     * If the specified Tuple is -not- part of the current result Tuple, the 
     *   method lookedAhead() should be called and the sourceTuple will be
     *   passed to the integrate method again when Cursor movement resumes.
     * 
     * @return true when a new result Tuple is available, or false if
     *   no result Tuple is available.
     */
    protected abstract boolean integrate()
      throws DataException;
    
    protected final boolean integrate(Ts tuple)
      throws DataException
    {
      sourceChannel.push(tuple);
      try
      { return integrate();
      }
      finally
      { sourceChannel.pop();
      }
    }
    
    protected final void lookedAhead()
    { lookahead+=direction;
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

  abstract class UnaryBoundQueryScrollableCursor
    extends UnaryBoundQuerySerialCursor
    implements ScrollableCursor<Tt>
  {
    protected final ScrollableCursor<Ts> sourceCursor;

    public UnaryBoundQueryScrollableCursor(ScrollableCursor<Ts> sourceCursor)
      throws DataException
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
