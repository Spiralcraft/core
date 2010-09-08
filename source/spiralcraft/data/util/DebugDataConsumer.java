//
// Copyright (c) 2007 Michael Toth
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

import java.io.PrintStream;

import spiralcraft.data.DataConsumer;
import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;

import spiralcraft.data.access.DataConsumerChain;

/**
 * A DataConsumer which prints data and events in a debugging friendly format
 *   to a PrintStream.
 *
 */
public class DebugDataConsumer<T extends Tuple>
    implements DataConsumerChain<T>
{
  
  private DataConsumer<T> nextConsumer;
  
  private PrintStream out=System.err;

  public DebugDataConsumer()
  { }
  
  public DebugDataConsumer(DataConsumer<T> nextConsumer)
  { this.nextConsumer=nextConsumer;
  }
  
  @Override
  @SuppressWarnings("unchecked") // Chains can convert between tuple types
  public void insertDataConsumer(DataConsumerChain<?> consumerChain)
  { 
    if (nextConsumer!=null)
    { consumerChain.setDataConsumer(nextConsumer);
    }
    nextConsumer=(DataConsumerChain<T>) consumerChain;
  }

  @Override
  @SuppressWarnings("unchecked") // Chains can convert between tuple types
  public void setDataConsumer(DataConsumer<?> consumer)
  { nextConsumer=(DataConsumer<T>) consumer;
  }

  @Override
  public void dataAvailable(T tuple) throws DataException
  {
    out.println(tuple.toText("| "));
    if (nextConsumer!=null)
    { nextConsumer.dataAvailable(tuple);
    }
  }

  @Override
  public void dataFinalize() throws DataException
  {
    out.println("DebugDataConsumer:dataFinalize()");
    if (nextConsumer!=null)
    { nextConsumer.dataFinalize();
    }
  }

  @Override
  public void dataInitialize(FieldSet fieldSet) throws DataException
  {
    out.println("DebugDataConsumer:dataInitialize(): "+fieldSet.toString());
    if (nextConsumer!=null)
    { nextConsumer.dataInitialize(fieldSet);
    }
  }
  
  @Override
  public void setDebug(boolean debug)
  { nextConsumer.setDebug(debug);
  }

}
