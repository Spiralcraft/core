//
//Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.stream;

import java.io.IOException;
import java.net.URI;

/**
 * <P>Provides a means for a component to translate from one 
 *   Resource to another.
 *   
 * <P>Allows a container that handles streaming resources, such as a web server
 *   or content management tool, to provide generic translation services and
 *   delegate implementation specifics to the deployment data.
 *
 */
public interface Translator
{
  Resource translate(Resource original,URI translatedURI)
    throws IOException,TranslationException;
}
