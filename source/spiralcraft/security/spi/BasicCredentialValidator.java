//
//Copyright (c) 2010 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.security.spi;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;

/**
 * A CredentialValidator implementation that uses Expression Bindings
 * 
 * @author mike
 *
 */
public class BasicCredentialValidator
  implements CredentialValidator
{

  protected Binding<Boolean> whenX;
  protected Binding<Boolean> validationX;
  
  public BasicCredentialValidator()
  {
  }
  
  public BasicCredentialValidator(Binding<Boolean> validationX)
  { this.validationX=validationX;
  }
  
  public BasicCredentialValidator
    (Binding<Boolean> whenX,Binding<Boolean> validationX)
  {
    this.whenX=whenX;
    this.validationX=validationX;
  }
  
  /**
   * <p>Determines when this validator is applicable. A non-true evaluation
   *   result will cause the validate() method to return null.
   * </p>
   * 
   * <p>This is typically used to run validation logic only when a specific
   *   combination of credentials are present.
   * </p>
   * 
   * @param whenX
   */
  public void setWhenX(Binding<Boolean> whenX)
  { this.whenX=whenX;
  }
  
  /**
   * <p>Determines whether the applicable credentials are valid. Returns
   *   true if the credentials are valid, or false if validation failed.
   * </p>
   *   
   * <p>If whenX is supplied, a null result will cause the validate() method
   *   to return false, otherwise a null result will cause the validate()
   *   method to return null.
   * </p>
   * 
   * @param validationX
   */
  public void setValidationX(Binding<Boolean> validationX)
  { this.validationX=validationX;
  }
  
  @Override
  public Boolean validate()
  {
    if (whenX!=null)
    {
      if (Boolean.TRUE.equals(whenX.get()))
      { return Boolean.TRUE.equals(validationX.get());
      }
      else
      { return null;
      }
    }
    else
    { return validationX.get();
    }
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  {
    if (whenX!=null)
    { whenX.bind(focusChain);
    }
    if (validationX!=null)
    { validationX.bind(focusChain);
    }
    else
    { throw new BindException("validationX property is required");
    }
    return focusChain;
  }

  @Override
  public String toString()
  {
    return super.toString()+": "
      + (whenX!=null?" whenX="+whenX.getText():"")
      + (validationX!=null?" validationX="+validationX.getText():""
        );
  }
  
}
