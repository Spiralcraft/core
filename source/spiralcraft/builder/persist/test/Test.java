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
package spiralcraft.builder.persist.test;

import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionContext;

import spiralcraft.builder.persist.AssemblyClassSchemeResolver;

import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.Field;
import spiralcraft.tuple.Tuple;

import spiralcraft.tuple.sax.TupleReader;

import spiralcraft.sax.ParseFactory;

import java.net.URI;

import java.util.List;

/**
 * A Test to exercise the Tuple based configuration persistence mechanism of
 *   builder.
 */
public class Test
  implements Executable
{
  public void execute(ExecutionContext context,String[] args)
  {
    try
    {
      AssemblyClassSchemeResolver resolver
        =new AssemblyClassSchemeResolver();
        
      Scheme scheme
        =resolver.resolveScheme
          (URI.create("java:/spiralcraft/builder/test/SimpleWidget"));
      
      context.out().println(scheme);
      
      
      TupleReader reader=new TupleReader(resolver,null);
      new ParseFactory().parseURI
        (URI.create("java:/spiralcraft/builder/persist/test/TestData.obj.xml")
        ,reader
        );
      
      System.out.println(reader.getTupleList().size());
      for (Tuple tuple: reader.getTupleList())
      {
        context.out().println(tuple.getScheme());
        for (Field field: tuple.getScheme().getFields())
        { context.out().println(field.getName()+"="+tuple.get(field.getIndex()));
        }
      }


    }
    catch (Throwable x)
    { x.printStackTrace(context.err());
    }
  }
}