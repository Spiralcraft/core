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

import spiralcraft.data.DataConsumer;
import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Tuple;
import spiralcraft.data.kit.AbstractBatchService;
import spiralcraft.data.spi.ArrayDeltaTuple;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.data.transaction.WorkException;
import spiralcraft.data.transaction.WorkUnit;

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
public class UpdaterService<Tdata extends Tuple>
  extends AbstractBatchService<Tdata>
{
  
 
  @Override
  protected void processBatch(final ArrayList<Tdata> dataBuffer)
  {
    try
    {
      new WorkUnit<Void>()
      {

        @Override
        protected Void run()
         throws WorkException
        { 
        
          try
          {
            if (logLevel.isFine())
            { log.fine("Inserting "+dataBuffer.size()+" rows");
            }
            DataConsumer<DeltaTuple> updater
              =sessionChannel.get().getSpace().getUpdater(getDataType());
           
            updater.dataInitialize(getDataType().getFieldSet());
            try
            {
              for (Tdata fact:dataBuffer)
              { 
                if (logLevel.isFine())
                { log.fine("Inserting "+fact);
                }
                DeltaTuple tuple
                  =fact instanceof DeltaTuple
                   ?(DeltaTuple) fact
                   :new ArrayDeltaTuple(null,fact);
                updater.dataAvailable(tuple);
              }
            }
            finally
            { updater.dataFinalize();
            }
            if (logLevel.isFine())
            { log.fine("Inserted "+dataBuffer.size()+" rows");
            }
          }
          catch (DataException x)
          { 
            throw new WorkException
              ("Error inserting data",x
              );
          }
        
        
          // TODO Auto-generated method stub
          return null;
        }
      }.work();
    }
    catch (TransactionException x)
    {
      throw new RuntimeException
        (x);
    }
    
    
  }
   

}
