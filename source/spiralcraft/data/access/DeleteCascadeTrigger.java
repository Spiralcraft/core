//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.data.access;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.session.Buffer;
import spiralcraft.data.session.DataSession;
import spiralcraft.data.transaction.TransactionException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.LangUtil;

public class DeleteCascadeTrigger
  extends DeltaTrigger
{

  private Channel<DataSession> dataSessionChannel;
  private Channel<DeltaTuple> tupleChannel;
  private Channel<DataComposite> relationship;
  private Expression<DataComposite> relationExpression;
  
  { 
    setForDelete(true);
    setWhen(When.BEFORE);
  }
  
    
  public DeleteCascadeTrigger(Expression<DataComposite> relationExpression)
  { this.relationExpression=relationExpression;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  {
    tupleChannel=(Channel<DeltaTuple>) focusChain.getSubject();
    dataSessionChannel=LangUtil.findChannel(DataSession.class,focusChain);
    relationship=focusChain.bind(relationExpression);
    return focusChain;
  }
  
  
  @Override
  public DeltaTuple trigger()
    throws TransactionException
  {
    
    DeltaTuple dt=tupleChannel.get();
    DataComposite relative=relationship.get();
    try
    {
      Buffer buf=dataSessionChannel.get().buffer(relative);
      buf.delete();
      buf.save();
    }
    catch (DataException x)
    { throw new TransactionException("Error cascading delete",x);
    }
    return dt;
  }


}
