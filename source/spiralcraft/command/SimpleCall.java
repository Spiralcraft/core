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
package spiralcraft.command;

public class SimpleCall<Tcontext,Tresult>
  extends Call<Tcontext,Tresult>
{
  private Tresult result;
  private Exception exception;
  
  public SimpleCall(String verb,Tcontext context)
  { super(verb,context);
  }
  
  @Override
  public void setResult(Tresult result)
  { this.result=result;
  }
  
  public Tresult getResult()
  { return result;
  }
  
  @Override
  public void setException(Exception exception)
  { this.exception=exception;
  }
  
  
  public Exception getException()
  { return exception;
  }
  
  @Override
  public String toString()
  { 
    return super.toString()+" result="+result
      +(exception==null?(" exception="+exception):null);
  }
}
