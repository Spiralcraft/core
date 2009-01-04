//
// Copyright (c) 2008,2009 Michael Toth
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
package spiralcraft.sax;

import org.xml.sax.SAXException;

import spiralcraft.util.Association;

public class AssociationReader
  extends ElementReader<Association<String,String>>
{

  private final String nameElement;
  private final String valueElement;
  protected boolean required;

  public AssociationReader(String nameElement,String valueElement)
  { 
    this.nameElement=nameElement;
    this.valueElement=valueElement;
  }
  
  @Override
  protected void close(Element element)
    throws SAXException
  { 
    Association<String,String> association=new Association<String,String>();

    Element nameElement=element.getChildByQName(this.nameElement);
    if (nameElement!=null)
    { association.setKey(CHARACTERS_READER.read(nameElement));
    }
    
    Element valueElement=element.getChildByQName(this.valueElement);
    if (valueElement!=null)
    { association.setValue(CHARACTERS_READER.read(valueElement));
    }
  
    if (required)
    {
      if (association.getKey()==null)
      { assertValueRequired(element,this.nameElement);
      }
      else if (association.getValue()==null)
      { assertValueRequired(element,this.valueElement);
      }
    }
    
    set(association);
    
  }
  
  protected void assertValueRequired(Element element,String childName)
    throws SAXException
  { throw new SAXException("Value required for "+childName+" in "+element);
  }
    
  
}
