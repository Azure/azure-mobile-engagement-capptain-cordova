/*
 * Copyright 2014 Capptain
 * 
 * Licensed under the CAPPTAIN SDK LICENSE (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *   https://app.capptain.com/#tos
 *  
 * This file is supplied "as-is." You bear the risk of using it.
 * Capptain gives no express or implied warranties, guarantees or conditions.
 * You may have additional consumer rights under your local laws which this agreement cannot change.
 * To the extent permitted under your local laws, Capptain excludes the implied warranties of merchantability,
 * fitness for a particular purpose and non-infringement.
 */

package com.ubikod.capptain.android.sdk.reach;

import static com.ubikod.capptain.android.sdk.reach.CapptainReachAgent.REACH_NAMESPACE;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.text.TextUtils;

/** Helper class for manipulating DOM elements */
final class XmlUtil
{
  /** XML parser configured to handle namespaces */
  private static final DocumentBuilderFactory sXmlFactory = DocumentBuilderFactory.newInstance();
  static
  {
    sXmlFactory.setNamespaceAware(true);
  }

  /* Prevent instance */
  private XmlUtil()
  {
    /* Nothing to do */
  }

  /** @return a DOM element from a XML string */
  static Element parseContent(String xml) throws ParserConfigurationException, SAXException,
    IOException
  {
    DocumentBuilder documentBuilder = sXmlFactory.newDocumentBuilder();
    Document document = documentBuilder.parse(new InputSource(new StringReader(xml)));
    return document.getDocumentElement();
  }

  /**
   * Unlike {@link Element#getAttribute(String)}, this method returns null when the attribute is
   * missing instead of the empty string.
   */
  static String getAttribute(Element element, String name)
  {
    String value = element.getAttribute(name);
    if (value.length() == 0)
      return null;
    else
      return value;
  }

  /**
   * Parse a boolean attribute.
   * @param element XML element having the attribute.
   * @param name attribute name.
   * @param defaultValue default value if the attribute is not set.
   * @return attribute boolean value.
   */
  static boolean getBooleanAttribute(Element element, String name, boolean defaultValue)
  {
    String value = element.getAttribute(name);
    if ("".equals(value))
      return defaultValue;
    return Boolean.parseBoolean(value);
  }

  /**
   * Get the first occurrence of a child tag.
   * @param element root tag to perform the search.
   * @param tag child tag
   * @param parentTag child's direct parent tag (optional)
   * @return the first occurrence of a child tag, or null if none has been found.
   */
  static Element getTag(Element element, String tag, String parentTag)
  {
    NodeList nodes = element.getElementsByTagNameNS(REACH_NAMESPACE, tag);
    for (int i = 0; i < nodes.getLength(); i++)
    {
      Element node = (Element) nodes.item(i);
      if (parentTag != null)
      {
        Node parent = node.getParentNode();
        String parentNS = parent.getNamespaceURI();
        if (REACH_NAMESPACE.equals(parentNS))
        {
          String parentName = parent.getLocalName();
          if (parentName.equals(parentTag))
            return node;
        }
      }
      else
        return node;
    }
    return null;
  }

  /**
   * Get the first occurrence of a child tag's text content.
   * @param element root tag to perform the search.
   * @param tag child tag.
   * @param parentTag child's direct parent tag (optional)
   * @return the first occurrence of a child tag's text content, or null if none has been found.
   */
  static String getTagText(Element element, String tag, String parentTag)
  {
    Element child = getTag(element, tag, parentTag);
    if (child == null)
      return null;
    else
      return getText(child);
  }

  /**
   * Get the text content of a node.
   * @param node node containing text.
   * @return node's text.
   */
  static String getText(Node node)
  {
    /* Traverse children */
    StringBuilder textBuilder = new StringBuilder();
    NodeList childNodes = node.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++)
    {
      /* Get child node */
      Node child = childNodes.item(i);

      /* Append PCDATA or CDATA element */
      if (child instanceof Text)
        textBuilder.append(child.getNodeValue());

      /* Convert &#\d+ into the proper unicode character */
      else if (child instanceof EntityReference)
      {
        /* We expect this method to return a thing like #\d+ */
        String name = child.getNodeName();
        if (name != null && name.length() > 1 && name.charAt(0) == '#')
        {
          int entityValue;
          try
          {
            /* Hexadecimal entity */
            if (name.charAt(0) == 'x' || name.charAt(0) == 'X')
              entityValue = Integer.parseInt(name.substring(2), 16);

            /* Decimal entity */
            else
              entityValue = Integer.parseInt(name.substring(1), 10);

            /* Cast as Java character */
            textBuilder.append((char) entityValue);
          }
          catch (NumberFormatException e)
          {
            /* Skip */
          }
        }
      }
    }

    /* Map empty text to null */
    String text = textBuilder.toString();
    if (TextUtils.isEmpty(text))
      return null;
    else
      return text;
  }
}
