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

import spiralcraft.lang.parser.ExpressionParser;
import spiralcraft.lang.spi.BeanReflector;
import spiralcraft.lang.spi.Namespace;
import spiralcraft.lang.spi.NamespaceReflector;
import spiralcraft.lang.spi.SimpleChannel;

import spiralcraft.lang.Expression;
import spiralcraft.lang.ParseException;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;


import spiralcraft.log.ClassLogger;



import spiralcraft.util.Arguments;

public class ExpressionResolverTest
{

  private static final ClassLogger log
    =new ClassLogger(ExpressionResolverTest.class);
  
  private String _expression=null;
  private boolean _dump=false;
  private int _bindRepeats=0;
  private int _getRepeats=0;

  public static void main(String ... args)
  {
    final ExpressionResolverTest test
      =new ExpressionResolverTest();
    test.run(args);
  }
  
  public void run(String ... args)
  {
    new Arguments()
    {

      protected boolean processOption(String option)
      {
        if (option=="expression")
        { _expression=nextArgument();
        }
        else if (option=="dump")
        { _dump=true;
        }
        else if (option=="bindRepeats")
        { _bindRepeats=Integer.parseInt(nextArgument());
        }
        else if (option=="getRepeats")
        { _getRepeats=Integer.parseInt(nextArgument());
        }
        else
        { return false;
        }
        return true;
      }
    }.process(args,'-');

    ExpressionParser parser = new ExpressionParser();

    try 
    {
      long time=System.currentTimeMillis();
      Expression<Object> expression = parser.parse(_expression);
      log.fine("Initial read time "+(System.currentTimeMillis()-time));
      
      if (_dump)
      { 
        StringBuffer out=new StringBuffer();
        expression.dumpParseTree(out);
        log.fine(out.toString());
      }

      SimpleFocus<Namespace> focus=new SimpleFocus<Namespace>();
      
      NamespaceReflector defs=new NamespaceReflector();
      defs.register("test",BeanReflector.getInstance(String.class));
      Namespace namespace=new Namespace(defs);
      
      
      namespace.putOptic("test",new SimpleChannel<String>("testValue",true));

      focus.setSubject(new SimpleChannel<Namespace>(defs,namespace,false));

      time=System.currentTimeMillis();

      Channel<Object> channel=expression.bind(focus);

      log.fine("Bind time "+(System.currentTimeMillis()-time));

      if (_bindRepeats>0)
      {
        time=System.currentTimeMillis();      
        for (int i=0;i<_bindRepeats;i++)
        { expression.bind(focus);
        }
        log.fine(_bindRepeats+" repeats bind time "+(System.currentTimeMillis()-time));
      }

      System.out.println(channel.get());      

      if (_getRepeats>0)
      {
        time=System.currentTimeMillis();      
        for (int i=0;i<_getRepeats;i++)
        { channel.get();
        }
        log.fine(_getRepeats+" repeats get time "+(System.currentTimeMillis()-time));
      }
    } 
    catch (ParseException e) 
    { 
      log.fine(e.getMessage());
      e.printStackTrace();

    }
    catch (BindException e) 
    { 
      log.fine(e.getMessage());
      e.printStackTrace();

    }

  }

}
