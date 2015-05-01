// ================================================================================================
//  CP_TBXML.h
//  Fast processing of XML files
//
// ================================================================================================
//  Created by Tom Bradley on 21/10/2009.
//  Version 1.4
//  
//  Copyright (c) 2009 Tom Bradley
//  
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//  
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//  
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.
// ================================================================================================

#import <Foundation/Foundation.h>

// ================================================================================================
//  Defines
// ================================================================================================
#define MAX_ELEMENTS 100
#define MAX_ATTRIBUTES 100

#define CP_TBXML_ATTRIBUTE_NAME_START 0
#define CP_TBXML_ATTRIBUTE_NAME_END 1
#define CP_TBXML_ATTRIBUTE_VALUE_START 2
#define CP_TBXML_ATTRIBUTE_VALUE_END 3
#define CP_TBXML_ATTRIBUTE_CDATA_END 4

// ================================================================================================
//  Structures
// ================================================================================================
typedef struct _CP_TBXMLAttribute {
    char * name;
    char * value;
    struct _CP_TBXMLAttribute * next;
} CP_TBXMLAttribute;

typedef struct _CP_TBXMLElt {
    char * name;
    char * text;
    
    CP_TBXMLAttribute * firstAttribute;
    
    struct _CP_TBXMLElt * parentElement;
    
    struct _CP_TBXMLElt * firstChild;
    struct _CP_TBXMLElt * currentChild;
    
    struct _CP_TBXMLElt * nextSibling;
    struct _CP_TBXMLElt * previousSibling;
    
} CP_TBXMLElt;

typedef struct _CP_TBXMLEltBuffer {
    CP_TBXMLElt * elements;
    struct _CP_TBXMLEltBuffer * next;
    struct _CP_TBXMLEltBuffer * previous;
} CP_TBXMLEltBuffer;

typedef struct _CP_TBXMLAttributeBuffer {
    CP_TBXMLAttribute * attributes;
    struct _CP_TBXMLAttributeBuffer * next;
    struct _CP_TBXMLAttributeBuffer * previous;
} CP_TBXMLAttributeBuffer;

// ================================================================================================
//  CP_TBXML Public Interface
// ================================================================================================
@interface CP_TBXML : NSObject {
    
@private
    CP_TBXMLElt * rootXMLElement;
    
    CP_TBXMLEltBuffer * currentElementBuffer;
    CP_TBXMLAttributeBuffer * currentAttributeBuffer;
    
    long currentElement;
    long currentAttribute;
    
    char * bytes;
    long bytesLength;
}

@property (nonatomic, readonly) CP_TBXMLElt * rootXMLElement;

+ (id)tbxmlWithURL:(NSURL*)aURL;
+ (id)tbxmlWithXMLString:(NSString*)aXMLString;
+ (id)tbxmlWithXMLData:(NSData*)aData;

- (id)initWithURL:(NSURL*)aURL;
- (id)initWithXMLString:(NSString*)aXMLString;
- (id)initWithXMLData:(NSData*)aData;

@end

// ================================================================================================
//  CP_TBXML Static Functions Interface
// ================================================================================================

@interface CP_TBXML (StaticFunctions)

+ (NSString*) elementName:(CP_TBXMLElt*)aXMLElement;
+ (NSString*) textForElement:(CP_TBXMLElt*)aXMLElement;
+ (NSString*) valueOfAttributeNamed:(NSString *)aName forElement:(CP_TBXMLElt*)aXMLElement;

+ (NSString*) attributeName:(CP_TBXMLAttribute*)aXMLAttribute;
+ (NSString*) attributeValue:(CP_TBXMLAttribute*)aXMLAttribute;

+ (CP_TBXMLElt*) nextSiblingNamed:(NSString*)aName searchFromElement:(CP_TBXMLElt*)aXMLElement;
+ (CP_TBXMLElt*) childElementNamed:(NSString*)aName parentElement:(CP_TBXMLElt*)aParentXMLElement;

+ (NSString*) unescapeXML:(NSString*)source;

@end
