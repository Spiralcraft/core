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


import spiralcraft.common.ContextualException;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Key;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.EquiJoin;
import spiralcraft.data.spi.KeyIdentifier;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;

import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.log.ClassLog;

/**
 * Returns a Buffer of a source by primary key. The Channel source functions as
 *   the parameter set for an Equijoin query.
 * 
 * @author mike
 *
 */
public class KeyBufferChannel<Tsource,Tbuffer extends Buffer>
  extends SourcedChannel<Tsource,Tbuffer>
{
  private static final ClassLog log=ClassLog.getInstance(BufferChannel.class);
  
  private Type<?> originalType;
  private Type<?> bufferType;
  private Channel<DataSession> sessionChannel;
  private Channel<?>[] keyChannels;
  private ThreadLocalChannel<Tsource> paramChannel;
  private BoundQuery<?,Tuple> boundQuery;
  private boolean unique;

/*
  private Channel<Buffer> parentChannel;
*/

  /**
   * Construct a KeyBufferChannel which buffers a Tuple retrieved by
   *   primary key.
   * 
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static final <Tsource,Tbuffer extends Buffer> 
    KeyBufferChannel<Tsource,Tbuffer> create
      (Type<?> originalType
      ,Channel<?> sourceChannel
      ,Expression<?>[] keyBindings
      ,Focus<?> focus
      )
    throws ContextualException
  {
    Key<?> pkey=originalType.getPrimaryKey();
    Expression<?>[] keyExpressions=pkey.getTargetExpressions();
    return new KeyBufferChannel
      (originalType,sourceChannel,keyExpressions,keyBindings,true,focus);
  }

  /**
   * <p>Construct a KeyBufferChannel using an set of keyExpressions which
   *   define the key of the set to buffer
   * </p>
   * 
   */
  public KeyBufferChannel
    (Type<?> originalType
    ,Channel<Tsource> paramChannel
    ,Expression<?>[] keyExpressions
    ,Expression<?>[] keyBindings
    ,boolean unique
    ,Focus<?> focus
    )
    throws ContextualException
  { 
    // Our Focus provides access to our containing scope,
    //   not the originalChannel

    super(DataReflector.<Tbuffer>getInstance(Type.getBufferType(originalType))
          ,paramChannel
          );
    this.unique=unique;
    this.originalType=originalType;
    this.bufferType=Type.getBufferType(originalType);
    if (debug)
    { log.fine("BufferChannel "+getReflector());
    }
    this.paramChannel=new ThreadLocalChannel<Tsource>(paramChannel,true);
    Focus<Tsource> paramFocus=focus.chain(this.paramChannel);
    
    if (originalType.getPrimaryKey()==null)
    { 
      throw new DataException
        ("Type "+originalType.getURI()+" does not have a primary key");
    }
    
    EquiJoin query
      =new EquiJoin
        (originalType
        ,originalType.getPrimaryKey().getTargetExpressions()
        ,keyBindings
        );
    boundQuery=LangUtil.assertInstance(Space.class,focus)
        .query(query,paramFocus);
    
    keyChannels=bind(keyBindings,paramFocus);
    setupSession(focus);

  }

  private void setupSession(Focus<?> focus)
    throws BindException
  {
    if (focus==null)
    { throw new IllegalArgumentException("Focus can't be null");
    }
    sessionChannel=DataSession.findChannel(focus);
    if (sessionChannel==null)
    { throw new BindException("Can't find a DataSession in Focus chain");
    }
    
    
  }
  

  @Override
  public boolean isWritable()
  { return false;
  }
  
  @Override
  public boolean store(Buffer val)
  { 
    if (debug)
    { log.fine("Not storing Buffer "+val);
    }
    return false;
  }

  @Override
  public Tbuffer retrieve()
  { 
    paramChannel.push();
    KeyIdentifier<?> id=null;
    try
    {
      id=KeyIdentifier.read(originalType,keyChannels);
      DataComposite original;
      if (unique)
      { original=BoundQuery.<Tuple>fetchUnique(boundQuery);
      }
      else
      { original=BoundQuery.<Tuple>fetch(boundQuery);
      }
      @SuppressWarnings("unchecked")
      Tbuffer buffer
        =(Tbuffer) sessionChannel.get().bufferForId(bufferType,id,original);
      return buffer;
    }
    catch (DataException x)
    { throw new AccessException("Error buffering "+originalType.getURI()+";"+id,x);
    }   
    finally
    { paramChannel.pop();
    }
    

  }

}