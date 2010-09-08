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

import spiralcraft.exec.Arguments;
import spiralcraft.exec.Executable;
import spiralcraft.lang.Expression;
import spiralcraft.lang.ParseException;
import spiralcraft.lang.parser.ExpressionParser;



public class ExpressionParserTest
  implements Executable
{

  private String _expression=null;
  private boolean _dump=false;
  private int _repeats=0;

  @Override
  public void execute(String ... args)
  {
    final ExpressionParserTest test=new ExpressionParserTest();
    test.run(args);
  }
  
  public void run(String[] args)
  {
    new Arguments()
    {

      @Override
      protected boolean processOption(String option)
      {
        if (option=="-expression")
        { _expression=nextArgument();
        }
        else if (option=="-dump")
        { _dump=true;
        }
        else if (option=="-repeats")
        { _repeats=Integer.parseInt(nextArgument());
        }
        else
        { return false;
        }
        return true;
      }
    }.process(args);

    ExpressionParser parser = new ExpressionParser();

    try 
    {
      long time=System.currentTimeMillis();
      Expression<?> expression = parser.parse(_expression);
      System.err.println("Initial read time "+(System.currentTimeMillis()-time));
      
      if (_dump)
      { 
        StringBuffer out=new StringBuffer();
        expression.dumpParseTree(out);
        System.err.println(out.toString());
      }

      if (_repeats>0)
      {
        time=System.currentTimeMillis();      
        for (int i=0;i<_repeats;i++)
        { expression=parser.parse(_expression);
        }
        System.err.println(_repeats+" repeats time "+(System.currentTimeMillis()-time));
      }
    } 
    catch (ParseException e) 
    { 
      System.err.println(e.getMessage());
      e.printStackTrace();

    }

  }

}
