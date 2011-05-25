//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.data.util;

import spiralcraft.common.ContextualException;
import spiralcraft.data.DataConsumer;
import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.editor.TupleEditor;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.session.BufferTuple;
import spiralcraft.data.session.PrimaryKeyBufferChannel;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.Transaction.Nesting;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.BindingChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.spi.ViewCache;
import spiralcraft.log.ClassLog;

/**
 * Summarizes incoming data to persistent storage
 * 
 * @author mike
 *
 */
public class Summarizer<T extends Tuple>
  implements Contextual,DataConsumer<T>
{
  @SuppressWarnings("unused")
  private static final ClassLog log
    =ClassLog.getInstance(Summarizer.class);
  
  private Type<T> factType;
  private Type<?> summaryType;
  private Expression<?>[] summaryKeyBindings;
  private Expression<?>[] summaryDataBindings;
  private BindingChannel<?>[] summaryDataChannels;
  

  private ThreadLocalChannel<T> fact;
  private PrimaryKeyBufferChannel<T,BufferTuple> summaryBuffer;

  private TupleEditor summaryEditor;
  private ViewCache viewCache;
  

  public void setFactType(Type<T> factType)
  { this.factType=factType;
  }
  
  public void setSummaryType(Type<?> summaryType)
  { this.summaryType=summaryType;
  }
  
  public Type<?> getSummaryType()
  { return this.summaryType;
  }
  
  public Type<T> getFactType()
  { return this.factType;
  }
  
  public void setSummaryKeyBindings(Expression<?>[] summaryKeyBindings)
  { this.summaryKeyBindings=summaryKeyBindings;
  }
  
  public void setSummaryDataBindings(Expression<?>[] summaryDataBindings)
  { this.summaryDataBindings=summaryDataBindings;
  }
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  {

    fact=new ThreadLocalChannel<T>(DataReflector.<T>getInstance(factType));
    fact.setContext(focusChain);
    
    Focus<?> factFocus=focusChain.chain(fact);
    
    viewCache=new ViewCache(focusChain);
    factFocus.addFacet(viewCache.bind(factFocus));
    
    summaryBuffer=new PrimaryKeyBufferChannel<T,BufferTuple>
      (summaryType 
      ,fact
      ,summaryKeyBindings
      ,factFocus
      );
        
    summaryEditor=new TupleEditor();
    summaryEditor.setSource(summaryBuffer);
    
    summaryDataChannels=BindingChannel.bind(summaryDataBindings,factFocus);
    summaryEditor.setPreSaveBindings
      (summaryDataChannels);
    
    summaryEditor.bind(focusChain);
    
    
    return focusChain;
  }
  
  @Override
  public void dataInitialize(
    FieldSet fieldSet)
    throws DataException
  {
    Transaction.startContextTransaction(Nesting.PROPOGATE);
  }

  @Override
  public void dataAvailable(T factItem)
    throws DataException
  {
    fact.push(factItem);
    summaryEditor.push();
    try
    {
      summaryEditor.initBuffer();
      
      // log.fine("FactCount="+summaryEditor.getBuffer());
      viewCache.push();
      try
      {
        viewCache.init();
      
        BindingChannel.applyReverse(summaryDataChannels);

        // log.fine("ViewCache "+ArrayUtil.format(viewCache.get(),",",""));
        // Trigger ViewCache
        viewCache.touch();
        viewCache.checkpoint();
      
        // Write updated summary values back to summary tuple
        summaryEditor.save(false);

        // log.fine("FactCount updated="+summaryEditor.getBuffer());
      }
      finally
      { viewCache.pop();
      }
    }
    catch (DataException x)
    { 
      Transaction.getContextTransaction().rollbackOnComplete();
      throw x;
    }
    finally
    {
      summaryEditor.pop();
      fact.pop();
    }
    
  }

  @Override
  public void dataFinalize()
    throws DataException
  { Transaction.getContextTransaction().complete();
  }

  @Override
  public void setDebug(
    boolean debug)
  { 
    
  }


  
}
