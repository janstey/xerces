/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999,2000 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.impl.validation.grammars;

import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.validation.Grammar;
import org.apache.xerces.impl.validation.ContentModelValidator;
import org.apache.xerces.impl.validation.XMLElementDecl;
import org.apache.xerces.impl.validation.XMLAttributeDecl;
import org.apache.xerces.impl.validation.XMLNotationDecl;
import org.apache.xerces.impl.validation.XMLEntityDecl;
import org.apache.xerces.impl.validation.XMLSimpleType;
import org.apache.xerces.impl.validation.XMLContentSpec;
import org.apache.xerces.impl.validation.datatypes.DatatypeValidatorFactoryImpl;
import org.apache.xerces.xni.QName;
import org.xml.sax.SAXException;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * @author Eric Ye, IBM
 * @author Stubs generated by DesignDoc on Mon Sep 11 11:10:57 PDT 2000
 * @author Jeffrey Rodriguez, IBM
 *
 * @version $Id$
 */
public class DTDGrammar
extends Grammar
implements XMLDTDHandler, XMLDTDContentModelHandler{

   //
   // Data
   //


   /** Chunk shift. */
   private static final int CHUNK_SHIFT = 8; // 2^8 = 256

   /** Chunk size. */
   private static final int CHUNK_SIZE = (1 << CHUNK_SHIFT);

   /** Chunk mask. */
   private static final int CHUNK_MASK = CHUNK_SIZE - 1;

   /** Initial chunk count. */
   private static final int INITIAL_CHUNK_COUNT = (1 << (10 - CHUNK_SHIFT)); // 2^10 = 1k

   /** Current ElementIndex */
   private int              fCurrentElementIndex;

   /** Element declaration. */
   private XMLElementDecl    fElementDecl        = new XMLElementDecl();

   /** Current AttributeIndex */
   private int               fCurrentAttributeIndex;

   /** Attribute declaration. */
   private XMLAttributeDecl  fAttributeDecl      = new XMLAttributeDecl();

   /** QName holder           */
   private QName             fQName              = new QName();

   /** XMLEntityDecl. */
   private XMLEntityDecl     fEntityDecl         = new XMLEntityDecl();

   /** internal XMLEntityDecl. */
   private XMLEntityDecl     fInternalEntityDecl = new XMLEntityDecl();

   /** external XMLEntityDecl */
   private XMLEntityDecl     fExternalEntityDecl = new XMLEntityDecl();

   /** Simple Type. */
   private XMLSimpleType     fSimpleType         = new XMLSimpleType();

   /** Content spec node. */
   private XMLContentSpec    fContentSpec        = new XMLContentSpec();

   /** fReadingExternalDTD */
   boolean fReadingExternalDTD = false;


   /** table of XMLAttributeDecl */
   Hashtable  fAttributeDeclTab   = new Hashtable();

   /** table of XMLElementDecl   */
   Hashtable   fElementDeclTab     = new Hashtable();

   /** table of XMLNotationDecl  */
   Hashtable  fNotationDeclTab    = new Hashtable();

   /** table of XMLSimplType     */
   Hashtable   fSimpleTypeTab     = new Hashtable();

   /** table of XMLEntityDecl    */
   Hashtable   fEntityDeclTab     = new Hashtable();

   /** Children Content Model  Stack */

   private short[] fOpStack     = null;
   private int[] fNodeIndexStack     = null;
   private int[] fPrevNodeIndexStack = null;

   /** Stack depth   */

    private int fDepth               = 0;


    /** fErrorReporter */
    private XMLErrorReporter fErrorReporter = null; 

    // additional fields(columns) for the element Decl pool in the Grammar

    /** flag if the elementDecl is External. */
    private int fElementDeclIsExternal[][] = new int[INITIAL_CHUNK_COUNT][];


    // additional fields(columns) for the attribute Decl pool in the Grammar

    /** flag if the AttributeDecl is External. */
    private int fAttributeDeclIsExternal[][] = new int[INITIAL_CHUNK_COUNT][];


    /** Mapping for attribute declarations. */
   // debugging

   /** Debug DTDGrammar. */
   private static final boolean DEBUG = false;

   //
   // Constructors
   //

   /** Default constructor. */
   public DTDGrammar() {
      this( "" );
   }

   /**
    * 
    * 
    * @param targetNamespace 
    */
   public DTDGrammar(String targetNamespace) {
      setTargetNameSpace( targetNamespace );
   }


   /** set up the ErrorReporter */
   public void setErrorReporter(XMLErrorReporter errorReporter) {
       fErrorReporter = errorReporter;
   }
   //
   // XMLDTDHandler methods
   //

   /**
    * This method notifies of the start of an entity. The DTD has the 
    * pseudo-name of "[dtd]; and parameter entity names start with '%'.
    * <p>
    * <strong>Note:</strong> Since the DTD is an entity, the handler
    * will be notified of the start of the DTD entity by calling the
    * startEntity method with the entity name "[dtd]" <em>before</em> calling
    * the startDTD method.
    * 
    * @param name     The name of the entity.
    * @param publicId The public identifier of the entity if the entity
    *                 is external, null otherwise.
    * @param systemId The system identifier of the entity if the entity
    *                 is external, null otherwise.
    * @param encoding The auto-detected IANA encoding name of the entity
    *                 stream. This value will be null in those situations
    *                 where the entity encoding is not auto-detected (e.g.
    *                 internal parameter entities).
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void startEntity(String name, String publicId, String systemId, 
                           String encoding) throws SAXException {
      XMLEntityDecl  entityDecl = new XMLEntityDecl();
      entityDecl.setValues(name,publicId,systemId, null, null, false);
      fEntityDeclTab.put( name, entityDecl );


      if (name.equals("[dtd]")) {
          fReadingExternalDTD = true;
      }

   }

   /**
    * Notifies of the presence of a TextDecl line in an entity. If present,
    * this method will be called immediately following the startEntity call.
    * <p>
    * <strong>Note:</strong> This method is only called for external
    * parameter entities referenced in the DTD.
    * 
    * @param version  The XML version, or null if not specified.
    * @param encoding The IANA encoding name of the entity.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void textDecl(String version, String encoding) throws SAXException {
   }

   /**
    * The start of the DTD.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void startDTD() throws SAXException {
       //Initialize stack
       fOpStack = null;
       fNodeIndexStack = null;
       fPrevNodeIndexStack = null;
   } // startDTD

   /**
    * A comment.
    * 
    * @param text The text in the comment.
    *
    * @throws SAXException Thrown by application to signal an error.
    */
   public void comment(XMLString text) throws SAXException {
   } // comment

   /**
    * A processing instruction. Processing instructions consist of a
    * target name and, optionally, text data. The data is only meaningful
    * to the application.
    * <p>
    * Typically, a processing instruction's data will contain a series
    * of pseudo-attributes. These pseudo-attributes follow the form of
    * element attributes but are <strong>not</strong> parsed or presented
    * to the application as anything other than text. The application is
    * responsible for parsing the data.
    * 
    * @param target The target.
    * @param data   The data or null if none specified.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void processingInstruction(String target, XMLString data)
   throws SAXException {
   } // processingInstruction

   /**
    * The start of the external subset.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void startExternalSubset() throws SAXException {
   } // startExternalSubset

   /**
    * The end of the external subset.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void endExternalSubset() throws SAXException {
   } // endExternalSubset

   /**
    * An element declaration.
    * 
    * @param name         The name of the element.
    * @param contentModel The element content model.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void elementDecl(String name, String contentModel)
   throws SAXException {
      XMLElementDecl tmpElementDecl = (XMLElementDecl) fElementDeclTab.get(name) ;

      // check if it is already defined
      if ( tmpElementDecl != null ) {
          if (tmpElementDecl.type == -1) {
              fCurrentElementIndex = getElementDeclIndex(name, -1);
          }
          else {
              fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                         "MSG_ELEMENT_ALREADY_DECLARED",
                                         new Object[]{ name },
                                         XMLErrorReporter.SEVERITY_ERROR);
              return;
          }
      }
      else {
          fCurrentElementIndex = createElementDecl();//create element decl
      }

      XMLElementDecl elementDecl       = new XMLElementDecl();
      QName          elementName       = new QName(null, name, name, null);
      //XMLSimpleType  elementSimpleType = new XMLSimpleType();

      elementDecl.name                  = elementName;

      elementDecl.contentModelValidator = null;
      elementDecl.scope= -1;
      if (contentModel.equals("EMPTY")) {
          elementDecl.type = XMLElementDecl.TYPE_EMPTY;
      }
      else if (contentModel.equals("ANY")) {
          elementDecl.type = XMLElementDecl.TYPE_ANY;
      }
      else if (contentModel.startsWith("(") ) {
          if (contentModel.indexOf("#PCDATA") > 0 ) {
              elementDecl.type = XMLElementDecl.TYPE_MIXED;
          }
          else {
              elementDecl.type = XMLElementDecl.TYPE_CHILDREN;
          }
      }


      //add(or set) this elementDecl to the local cache
      this.fElementDeclTab.put(name, elementDecl );

      fElementDecl         = elementDecl; 

      if (fDepth == 0 && fNodeIndexStack != null) {
          if (elementDecl.type == XMLElementDecl.TYPE_MIXED) {
              int pcdata = addUniqueLeafNode(null);
              if (fNodeIndexStack[0] == -1) {
                  fNodeIndexStack[0] = pcdata;
              }
              else {
                  fNodeIndexStack[0] = addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_CHOICE, 
                                                          pcdata, fNodeIndexStack[0]);
              }
          }
          setContentSpecIndex(fCurrentElementIndex, fNodeIndexStack[fDepth]);
      }

      if ( DEBUG ) {
          System.out.println(  "name = " + fElementDecl.name.localpart );
          System.out.println(  "Type = " + fElementDecl.type );
      }

      setElementDecl(fCurrentElementIndex, fElementDecl );//set internal structure

      int chunk = fCurrentElementIndex >> CHUNK_SHIFT;
      int index = fCurrentElementIndex & CHUNK_MASK;
      ensureElementDeclCapacity(chunk);
      fElementDeclIsExternal[chunk][index] = fReadingExternalDTD? 1 : 0;

   } // elementDecl

   /**
    * The start of an attribute list.
    * 
    * @param elementName The name of the element that this attribute
    *                    list is associated with.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void startAttlist(String elementName) throws SAXException {
   } // startAttlist

   /**
    * An attribute declaration.
    * 
    * @param elementName   The name of the element that this attribute
    *                      is associated with.
    * @param attributeName The name of the attribute.
    * @param type          The attribute type. This value will be one of
    *                      the following: "CDATA", "ENTITY", "ENTITIES",
    *                      "ENUMERATION", "ID", "IDREF", "IDREFS", 
    *                      "NMTOKEN", "NMTOKENS", or "NOTATION".
    * @param enumeration   If the type has the value "ENUMERATION", this
    *                      array holds the allowed attribute values;
    *                      otherwise, this array is null.
    * @param defaultType   The attribute default type. This value will be
    *                      one of the following: "#FIXED", "#IMPLIED",
    *                      "#REQUIRED", or null.
    * @param defaultValue  The attribute default value, or null if no
    *                      default value is specified.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void attributeDecl(String elementName, String attributeName, String type, String[] enumeration, String defaultType, XMLString defaultValue)
   throws SAXException {

      if ( this.fElementDeclTab.containsKey( (String) elementName) ) {
         //if ElementDecl has already being created in the Grammar then remove from table, 
         //this.fElementDeclTab.remove( (String) elementName );
      }
      // then it is forward reference to a element decl, create the elementDecl first.
      else {
          fCurrentElementIndex = createElementDecl();//create element decl

          XMLElementDecl elementDecl       = new XMLElementDecl();
          elementDecl.name = new QName(null, elementName, elementName, null);
          
          //add(or set) this elementDecl to the local cache
          this.fElementDeclTab.put(elementName, elementDecl );

          //set internal structure
          setElementDecl(fCurrentElementIndex, elementDecl );
      }

      //Get Grammar index to grammar array
      int elementIndex       = getElementDeclIndex( elementName, -1 );


      fCurrentAttributeIndex = createAttributeDecl();// Create current Attribute Decl

      fSimpleType.clear();
      if ( defaultType != null ) {
          if ( defaultType.equals( "FIXED") ) {
              fSimpleType.defaultType = fSimpleType.DEFAULT_TYPE_FIXED;
          } else if ( defaultType.equals( "IMPLIED") ) {
              fSimpleType.defaultType = fSimpleType.DEFAULT_TYPE_IMPLIED;
          } else if ( defaultType.equals( "REQUIRED") ) {
              fSimpleType.defaultType = fSimpleType.DEFAULT_TYPE_REQUIRED;
          }
         }
      if ( DEBUG == true ) {
          System.out.println("defaultvalue = " + defaultValue.toString() );
      }
      fSimpleType.defaultValue      = defaultValue.toString();
      fSimpleType.enumeration       = enumeration;
      fSimpleType.datatypeValidator = DatatypeValidatorFactoryImpl.getDatatypeRegistry().getDatatypeValidator(type);


      fQName.clear();
      fQName.setValues(null, attributeName, attributeName, null);


      fAttributeDecl.clear();
      fAttributeDecl.simpleType     = fSimpleType;
      fAttributeDecl.setValues( fQName, fSimpleType, false );

      /* System.out.println( "elementIndex = " + elementIndex ); */
      setAttributeDecl( elementIndex, fCurrentAttributeIndex,
                           fAttributeDecl );

      int chunk = fCurrentAttributeIndex >> CHUNK_SHIFT;
      int index = fCurrentAttributeIndex & CHUNK_MASK;
      ensureAttributeDeclCapacity(chunk);
      fAttributeDeclIsExternal[chunk][index] = fReadingExternalDTD ?  1 : 0;
   } // attributeDecl

/**
    * The end of an attribute list.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void endAttlist() throws SAXException {
   } // endAttlist

   /**
    * An internal entity declaration.
    * 
    * @param name The name of the entity. Parameter entity names start with
    *             '%', whereas the name of a general entity is just the 
    *             entity name.
    * @param text The value of the entity.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void internalEntityDecl(String name, XMLString text)
   throws SAXException {
   } // internalEntityDecl

   /**
    * An external entity declaration.
    * 
    * @param name     The name of the entity. Parameter entity names start
    *                 with '%', whereas the name of a general entity is just
    *                 the entity name.
    * @param publicId The public identifier of the entity or null if the
    *                 the entity was specified with SYSTEM.
    * @param systemId The system identifier of the entity.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void externalEntityDecl(String name, String publicId, String systemId)
   throws SAXException {
   } // externalEntityDecl

   /**
    * An unparsed entity declaration.
    * 
    * @param name     The name of the entity.
    * @param publicId The public identifier of the entity, or null if not
    *                 specified.
    * @param systemId The system identifier of the entity, or null if not
    *                 specified.
    * @param notation The name of the notation.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void unparsedEntityDecl(String name, String publicId, String systemId, String notation)
   throws SAXException {
   } // unparsedEntityDecl

   /**
    * A notation declaration
    * 
    * @param name     The name of the notation.
    * @param publicId The public identifier of the notation, or null if not
    *                 specified.
    * @param systemId The system identifier of the notation, or null if not
    *                 specified.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void notationDecl(String name, String publicId, String systemId)
   throws SAXException {
   } // notationDecl

   /**
    * The start of a conditional section.
    * 
    * @param type The type of the conditional section. This value will
    *             either be CONDITIONAL_INCLUDE or CONDITIONAL_IGNORE.
    *
    * @throws SAXException Thrown by handler to signal an error.
    *
    * @see CONDITIONAL_INCLUDE
    * @see CONDITIONAL_IGNORE
    */
   public void startConditional(short type) throws SAXException {
   } // startConditional

   /**
    * The end of a conditional section.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void endConditional() throws SAXException {
   } // endConditional

   /**
    * The end of the DTD.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void endDTD() throws SAXException {
      /*
      XMLElementDecl  elementDecl;
      Enumeration     elements       = fElementDeclTab.elements();
      int             elementDeclIdx = 0;
      while( elements.hasMoreElements() == true ){
         elementDecl    = (XMLElementDecl) elements.nextElement();
         elementDeclIdx = getElementDeclIndex( elementDecl.name );
         System.out.println( "elementDeclIdx = " + elementDeclIndex );
         if( elementDeclIndex != -1 ){   
             elementDecl.contentModelValidator = this.getElementContentModelValidator(elementDeclIdx );
         }
         fCurrentElementIndex = createElementDecl();//create element decl
         if ( DEBUG == true ) {
            System.out.println(  "name = " + fElementDecl.name.localpart );
            System.out.println(  "Type = " + fElementDecl.type );
         }
         setElementDecl(fCurrentElementIndex, fElementDecl );//set internal structure
      }
      }
      */
   } // endDTD

   /**
    * This method notifies the end of an entity. The DTD has the pseudo-name
    * of "[dtd]; and parameter entity names start with '%'.
    * <p>
    * <strong>Note:</strong> Since the DTD is an entity, the handler
    * will be notified of the end of the DTD entity by calling the
    * endEntity method with the entity name "[dtd]" <em>after</em> calling
    * the endDTD method.
    * 
    * @param name The name of the entity.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void endEntity(String name) throws SAXException {
       if (name.equals("[dtd]")) {
           fReadingExternalDTD = false;
       }
   }

   //
   // XMLDTDContentModelHandler methods
   //

   /**
    * The start of a content model. Depending on the type of the content
    * model, specific methods may be called between the call to the
    * startContentModel method and the call to the endContentModel method.
    * 
    * @param elementName The name of the element.
    * @param type        The content model type.
    *
    * @throws SAXException Thrown by handler to signal an error.
    *
    * @see TYPE_EMPTY
    * @see TYPE_ANY
    * @see TYPE_MIXED
    * @see TYPE_CHILDREN
    */
   public void startContentModel(String elementName, short type)
   throws SAXException {
      XMLElementDecl elementDecl = (XMLElementDecl) this.fElementDeclTab.get( elementName);
      if ( elementDecl != null ) {
         fElementDecl = elementDecl;
      }
      fDepth = 0;
      initializeContentModelStack();

   } // startContentModel

   /**
    * A referenced element in a mixed content model. If the mixed content 
    * model only allows text content, then this method will not be called
    * for that model. However, if this method is called for a mixed
    * content model, then the zero or more occurrence count is implied.
    * <p>
    * <strong>Note:</strong> This method is only called after a call to 
    * the startContentModel method where the type is TYPE_MIXED.
    * 
    * @param elementName The name of the referenced element. 
    *
    * @throws SAXException Thrown by handler to signal an error.
    *
    * @see TYPE_MIXED
    */
   int valueIndex            = -1;
   int prevNodeIndex         = -1;
   int nodeIndex             = -1;
   public void mixedElement(String elementName) throws SAXException {
       fNodeIndexStack[fDepth] = addUniqueLeafNode(elementName);
   } // mixedElement

   /**
    * The start of a children group.
    * <p>
    * <strong>Note:</strong> This method is only called after a call to
    * the startContentModel method where the type is TYPE_CHILDREN.
    * <p>
    * <strong>Note:</strong> Children groups can be nested and have
    * associated occurrence counts.
    *
    * @throws SAXException Thrown by handler to signal an error.
    *
    * @see TYPE_CHILDREN
    */
   public void childrenStartGroup() throws SAXException {
      fDepth++;
      initializeContentModelStack();
   } // childrenStartGroup

   /**
    * A referenced element in a children content model.
    * 
    * @param elementName The name of the referenced element.
    *
    * @throws SAXException Thrown by handler to signal an error.
    *
    * @see TYPE_CHILDREN
    */
   public void childrenElement(String elementName) throws SAXException {
       fNodeIndexStack[fDepth] = addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_LEAF, elementName);
   } // childrenElement

   /**
    * The separator between choices or sequences of a children content
    * model.
    * <p>
    * <strong>Note:</strong> This method is only called after a call to
    * the startContentModel method where the type is TYPE_CHILDREN.
    * 
    * @param separator The type of children separator.
    *
    * @throws SAXException Thrown by handler to signal an error.
    *
    * @see SEPARATOR_CHOICE
    * @see SEPARATOR_SEQUENCE
    * @see TYPE_CHILDREN
    */
   public void childrenSeparator(short separator) throws SAXException {
       if (fOpStack[fDepth] != XMLContentSpec.CONTENTSPECNODE_SEQ && separator == XMLDTDContentModelHandler.SEPARATOR_CHOICE ) {
           if (fPrevNodeIndexStack[fDepth] != -1) {
               fNodeIndexStack[fDepth] = addContentSpecNode(fOpStack[fDepth], fPrevNodeIndexStack[fDepth], fNodeIndexStack[fDepth]);
           }
           fPrevNodeIndexStack[fDepth] = fNodeIndexStack[fDepth];
           fOpStack[fDepth] = XMLContentSpec.CONTENTSPECNODE_CHOICE;
       } else if (fOpStack[fDepth] != XMLContentSpec.CONTENTSPECNODE_CHOICE && separator == XMLDTDContentModelHandler.SEPARATOR_SEQUENCE) {
           if (fPrevNodeIndexStack[fDepth] != -1) {
               fNodeIndexStack[fDepth] = addContentSpecNode(fOpStack[fDepth], fPrevNodeIndexStack[fDepth], fNodeIndexStack[fDepth]);
           }
            fPrevNodeIndexStack[fDepth] = fNodeIndexStack[fDepth];
            fOpStack[fDepth] = XMLContentSpec.CONTENTSPECNODE_SEQ;
       }
   } // childrenSeparator

   /**
    * The occurrence count for a child in a children content model.
    * <p>
    * <strong>Note:</strong> This method is only called after a call to
    * the startContentModel method where the type is TYPE_CHILDREN.
    * 
    * @param occurrence The occurrence count for the last children element
    *                   or children group.
    *
    * @throws SAXException Thrown by handler to signal an error.
    *
    * @see OCCURS_ZERO_OR_ONE
    * @see OCCURS_ZERO_OR_MORE
    * @see OCCURS_ONE_OR_MORE
    * @see TYPE_CHILDREN
    */
   public void childrenOccurrence(short occurrence) throws SAXException {
      if ( occurrence == XMLDTDContentModelHandler.OCCURS_ZERO_OR_ONE ) {
         fNodeIndexStack[fDepth] = addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE, fNodeIndexStack[fDepth], -1);
      } else if ( occurrence == XMLDTDContentModelHandler.OCCURS_ZERO_OR_MORE ) {
         fNodeIndexStack[fDepth] = addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE, fNodeIndexStack[fDepth], -1 );
      } else if ( occurrence == XMLDTDContentModelHandler.OCCURS_ONE_OR_MORE) {
         fNodeIndexStack[fDepth] = addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE, fNodeIndexStack[fDepth], -1 );
      }

   } // childrenOccurrence

   /**
    * The end of a children group.
    * <p>
    * <strong>Note:</strong> This method is only called after a call to
    * the startContentModel method where the type is TYPE_CHILDREN.
    *
    * @see TYPE_CHILDREN
    */
   public void childrenEndGroup() throws SAXException {
       if (fPrevNodeIndexStack[fDepth] != -1) {
           fNodeIndexStack[fDepth] = addContentSpecNode(fOpStack[fDepth], fPrevNodeIndexStack[fDepth], fNodeIndexStack[fDepth]);
       }
       int nodeIndex = fNodeIndexStack[fDepth--];
       fNodeIndexStack[fDepth] = nodeIndex;
   } // childrenEndGroup

   /**
    * The end of a content model.
    *
    * @throws SAXException Thrown by handler to signal an error.
    */
   public void endContentModel() throws SAXException {

   } // endContentModel


    // getters for isExternals 
    public boolean getElementDeclIsExternal(int elementDeclIndex) {
        if (elementDeclIndex < 0) {
            return false;
        }
        int chunk = elementDeclIndex >> CHUNK_SHIFT;
        int index = elementDeclIndex & CHUNK_MASK;
        return (fElementDeclIsExternal[chunk][index] != 0);
    }

    public boolean getAttributeDeclIsExternal(int attributeDeclIndex) {
        if (attributeDeclIndex < 0) {
            return false;
        }
        int chunk = attributeDeclIndex >> CHUNK_SHIFT;
        int index = attributeDeclIndex & CHUNK_MASK;
        return (fAttributeDeclIsExternal[chunk][index] != 0);
    }


   //
   //
   // private methods
   //
   //


   /**
  * Create an XMLContentSpec for a single non-leaf
  * 
  * @param nodeType the type of XMLContentSpec to create - from XMLContentSpec.CONTENTSPECNODE_*
  * @param nodeValue handle to an XMLContentSpec
  * @return handle to the newly create XMLContentSpec
  * @exception java.lang.Exception
  */

   private int addContentSpecNode(short nodeType, 
                                  String nodeValue)  {

      // create content spec node
      int contentSpecIndex = createContentSpec();

      // set content spec node values


      fContentSpec.setValues(nodeType, nodeValue, null);
      setContentSpec(contentSpecIndex, fContentSpec);

      // return index 
      return contentSpecIndex;

   } // addContentSpecNode(int,int):int


   /**
    * create an XMLContentSpec for a leaf
    *
    * @param   elementName  the name (Element) for the node
    * @return handle to the newly create XMLContentSpec
    * @exception java.lang.Exception
    */

   private int addUniqueLeafNode(String elementName) {

      // create content spec node
      int contentSpecIndex = createContentSpec();

      // set content spec node values

      fContentSpec.setValues( XMLContentSpec.CONTENTSPECNODE_LEAF, 
                              elementName, null);
      setContentSpec(contentSpecIndex, fContentSpec);

      // return index 
      return contentSpecIndex;

   } // addUniqueLeafNode(int):int

   /**
    * Create an XMLContentSpec for a two child leaf
    *
    * @param nodeType the type of XMLContentSpec to create - from XMLContentSpec.CONTENTSPECNODE_*
    * @param leftNodeIndex handle to an XMLContentSpec
    * @param rightNodeIndex handle to an XMLContentSpec
    * @return handle to the newly create XMLContentSpec
    * @exception java.lang.Exception
    */
   private int addContentSpecNode(short nodeType, 
                                  int leftNodeIndex, 
                                  int rightNodeIndex) {

      // create content spec node
      int contentSpecIndex = createContentSpec();

      // set content spec node values
      int[] leftIntArray  = new int[1]; 
      int[] rightIntArray = new int[1];

      leftIntArray[0]      = leftNodeIndex;
      rightIntArray[0]    = rightNodeIndex;
      fContentSpec.setValues(nodeType, 
                             leftIntArray, rightIntArray);

      setContentSpec(contentSpecIndex, fContentSpec);

      // return index 
      return contentSpecIndex;

   } // addContentSpecNode(int,int,int):int


   // intialize content model stack
   private void initializeContentModelStack() {
      if (fOpStack == null) {
         fOpStack = new short[8];
         fNodeIndexStack = new int[8];
         fPrevNodeIndexStack = new int[8];
      } else if (fDepth == fOpStack.length) {
         short[] newStack = new short[fDepth * 2];
         System.arraycopy(fOpStack, 0, newStack, 0, fDepth);
         fOpStack = newStack;
         int[]   newIntStack = new int[fDepth * 2];
         System.arraycopy(fNodeIndexStack, 0, newIntStack, 0, fDepth);
         fNodeIndexStack = newIntStack;
         newIntStack = new int[fDepth * 2];
         System.arraycopy(fPrevNodeIndexStack, 0, newIntStack, 0, fDepth);
         fPrevNodeIndexStack = newIntStack;
      }
      fOpStack[fDepth] = -1;
      fNodeIndexStack[fDepth] = -1;
      fPrevNodeIndexStack[fDepth] = -1;
   }

    
   // ensure capacity

    /** Ensures storage for element declaration mappings. */
    private boolean ensureElementDeclCapacity(int chunk) {
        try {
            return fElementDeclIsExternal[chunk][0] == 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            fElementDeclIsExternal = resize(fElementDeclIsExternal, 
                                     fElementDeclIsExternal.length * 2);
        } catch (NullPointerException ex) {
            // ignore
        }
        fElementDeclIsExternal[chunk] = new int[CHUNK_SIZE];
        return true;
    }

    /** Ensures storage for attribute declaration mappings. */
    private boolean ensureAttributeDeclCapacity(int chunk) {
        try {
            return fAttributeDeclIsExternal[chunk][0] == 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            fAttributeDeclIsExternal = resize(fAttributeDeclIsExternal, 
                                       fAttributeDeclIsExternal.length * 2);
        } catch (NullPointerException ex) {
            // ignore
        }
        fAttributeDeclIsExternal[chunk] = new int[CHUNK_SIZE];
        return true;
    }

    // resize initial chunk

    /** Resizes chunked integer arrays. */
    private int[][] resize(int array[][], int newsize) {
        int newarray[][] = new int[newsize][];
        System.arraycopy(array, 0, newarray, 0, array.length);
        return newarray;
    }


} // class DTDGrammar
