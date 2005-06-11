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
package spiralcraft.lang.parser;


public class ExclusiveOrNode
  extends Node
{

  private final Node _op1;
  private final Node _op2;

  public ExclusiveOrNode(Node op1,Node op2)
  { 
    _op1=op1;
    _op2=op2;
  }

  public void dumpTree(StringBuffer out,String prefix)
  { 
    out.append(prefix).append("Xor");
    prefix=prefix+"  ";
    _op1.dumpTree(out,prefix);
    out.append(prefix).append("^");
    _op2.dumpTree(out,prefix);
  }

}
