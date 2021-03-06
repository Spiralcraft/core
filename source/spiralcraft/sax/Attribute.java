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
package spiralcraft.sax;
  
/**
 * Represents an XML Attribute (a name-value pair specified within an element)
 */
public class Attribute
{
  private String _localName;
  private String _qName;
  private String _type;
  private String _uri;
  private String _value;

  /**
   * Simple constructor for no-namespace client use
   */
  public Attribute
    (String name
    ,String value
    )
  { 
    _localName=name;
    _qName=name;
    _type="";
    _uri=name;
    _value=value;

  }

  public Attribute
    (String localName
    ,String qName
    ,String type
    ,String uri
    ,String value
    )
  { 
    _localName=localName;
    _qName=qName;
    _type=type;
    _uri=uri;
    _value=value;
  }

  public String getLocalName()
  { return _localName;
  }

  public String getQName()
  { return _qName;
  }

  public String getType()
  { return _type;
  }

  public String getURI()
  { return _uri;
  }

  /**
   * @return A String in the format of: { uri "#" } name
   */
  public String getResolvedName()
  { return _uri!=null && !_uri.isEmpty()?_uri+"#"+_localName:_localName;
  }

  
  public String getValue()
  { return _value;
  }

  public void setValue(String value)
  { _value=value;
  }

  @Override
  public String toString()
  { 
    return super.toString()
      +"[uri="+_uri
      +",localName="+_localName
      +",qName="+_qName
      +",type="+_type
      +",value="+_value
      +"]"
      ;
  }

}
