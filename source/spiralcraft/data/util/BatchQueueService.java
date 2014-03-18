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
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.kit.AbstractBatchService;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.lang.kit.Callable;

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
public class BatchQueueService<Tdata extends Tuple>
  extends AbstractBatchService<Tdata>
{
  
  private Binding<Void> binding;
  private Callable<Tdata[],Void> fn;
  
  public void setProcessingFunction
    (Binding<Void> binding)
  { this.binding=binding;
  }
  
  @Override
  protected Focus<?> bindExports(Focus<?> focus)
    throws ContextualException
  {
    fn=new Callable<Tdata[],Void>
      (focus
      ,DataReflector.<Tdata[]>getInstance(Type.getArrayType(this.getDataType()))
      ,binding
      );
    return super.bindExports(focus);
  } 
  
  @SuppressWarnings("unchecked")
  @Override
  protected void processBatch(final ArrayList<Tdata> dataBuffer)
  { 
    fn.evaluate((Tdata[]) dataBuffer.toArray(new Tuple[dataBuffer.size()]));
  }
   

}
