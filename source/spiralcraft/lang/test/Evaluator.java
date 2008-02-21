//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.lang.test;


import spiralcraft.lang.Expression;
import spiralcraft.lang.ParseException;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;

import spiralcraft.lang.spi.SimpleChannel;

import spiralcraft.log.ClassLogger;

public class Evaluator
{

  static ClassLogger log=new ClassLogger(Evaluator.class);
  
  public static <X,Y> X parseAndEvaluateObject(String expression,Y subject)
    throws BindException,ParseException
  { 
    return Evaluator.<X,Y>parseAndEvaluateOptic
      (expression,new SimpleChannel<Y>(subject,true));
  }
  
  public static <X,Y> X parseAndEvaluateOptic(String expression,Channel<Y> subject)
    throws BindException,ParseException
  { 
    return Evaluator.<X,Y>evaluate
      (Expression.<X>parse(expression),new SimpleFocus<Y>(subject));
  }
  
  public static <X,Y> X evaluate(Expression<X> expression,Focus<Y> focus)
    throws BindException
  { 
    log.fine("evaluate:"+expression+":"+focus.bind(expression));
    return focus.<X>bind(expression).get();
  }
}
