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

import java.util.ArrayList;

import spiralcraft.common.ContextualException;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.kit.AbstractBatchService;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;

/**
 * <p>Asynchronously summarizes incoming data into persistent storage.
 * </p>
 * 
 * <p>Used to capture and record statistics on-the-fly
 * </p>
 * 
 * @author mike
 *
 */
public class SummarizerService<Tsummary extends Tuple,Tfact extends Tuple>
  extends AbstractBatchService<Tfact>
{
  
  private Summarizer<Tfact> summarizer=new Summarizer<Tfact>();
    
  /**
   * The data type which stores the summary
   * 
   * @param type
   */
  public void setSummaryType(Type<Tsummary> type)
  { summarizer.setSummaryType(type);
  }
  
  
  public void setSummaryKeyBindings(Expression<?>[] summaryKeyBindings)
  { summarizer.setSummaryKeyBindings(summaryKeyBindings);
  }
  
  public void setSummaryDataBindings(Expression<?>[] summaryDataBindings)
  { summarizer.setSummaryDataBindings(summaryDataBindings);
  }
  
  /**
   * The type of fact being summarized
   * 
   * @param factType
   */
  public void setFactType(Type<Tfact> factType)
  { setDataType(factType);
  }
 
  @Override
  protected void processBatch(ArrayList<Tfact> factBuffer)
  {
    try
    {
     
      summarizer.dataInitialize(summarizer.getFactType().getFieldSet());
      try
      {
        for (Tfact fact:factBuffer)
        { summarizer.dataAvailable(fact);
        }
      }
      finally
      { summarizer.dataFinalize();
      }
  
    }
    catch (DataException x)
    { 
      throw new RuntimeException
        ("Error summarizing data to "+summarizer.getSummaryType().getURI()
        ,x
        );
    }
    
  }
   
  @Override
  protected Focus<?> bindImports(
    Focus<?> focusChain)
    throws ContextualException
  { 
    summarizer.setFactType(getDataType());
    addExportContextual(summarizer);
    return super.bindImports(focusChain);
  }


}
