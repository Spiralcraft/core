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
import spiralcraft.lang.DefaultFocus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;

import spiralcraft.lang.optics.SimpleBinding;


public class Evaluator
{

  public static <X,Y> X parseAndEvaluateObject(String expression,Y subject)
    throws BindException,ParseException
  { 
    return Evaluator.<X,Y>parseAndEvaluateOptic
      (expression,new SimpleBinding<Y>(subject,true));
  }
  
  public static <X,Y> X parseAndEvaluateOptic(String expression,Optic<Y> subject)
    throws BindException,ParseException
  { 
    return Evaluator.<X,Y>evaluate
      (Expression.<X>parse(expression),new DefaultFocus<Y>(subject));
  }
  
  public static <X,Y> X evaluate(Expression<X> expression,Focus<Y> focus)
    throws BindException
  { return focus.<X>bind(expression).get();
  }
}
