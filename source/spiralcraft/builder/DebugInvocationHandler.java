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
package spiralcraft.builder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import spiralcraft.util.ArrayUtil;

/**
 * An InvocationHandler which reports method invocations to System.err and
 *   always returns null.
 */
public class DebugInvocationHandler
  implements InvocationHandler
{
  // XXX TO-DO: Move to new util.reflect package
  
  private final Class _interface;
  
  public DebugInvocationHandler(Class clazz)
  { _interface=clazz;
  }
  
  public Object invoke
    (Object proxy
    ,Method method,
    Object[] args
    )
    throws Throwable
  { 
    System.err.println
      (method.toString()
      +"("
      +ArrayUtil.formatToString(args,",","\"")
      +")"
      );
    return null;    
  }
}
