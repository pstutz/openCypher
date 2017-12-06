/*
 * Copyright (c) 2015-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
grammar FeatureResults;

value : node
      | relationship
      | path
      | integer
      | floatingPoint
      | string
      | bool
      | nullValue
      | list
      | map
      ;

node : '(' (label)* WS? (propertyMap)? ')' ;

relationship : '[' relationshipType WS? (propertyMap)? ']' ;

path : '<' node (pathSegment)* '>' ;

pathSegment : (forwardsRelationship | backwardsRelationship) node ;

forwardsRelationship : '-' relationship '->' ;

backwardsRelationship : '<-' relationship '-' ;

integer : INTEGER_LITERAL ;

floatingPoint : FLOAT_LITERAL
              | INFINITY ;

bool : 'true'
     | 'false'
     ;

nullValue : 'null' ;

list : '[' (values)? ']' ;

values : value (', ' value)* ;

map : propertyMap ;

propertyMap : '{' (keyValuePair (', ' keyValuePair)*)? '}' ;

keyValuePair: propertyKey ':' WS? value ;

propertyKey : SYMBOLIC_NAME ;

relationshipType : ':' SYMBOLIC_NAME ;

label : ':' SYMBOLIC_NAME ;

INTEGER_LITERAL : ('-')? DECIMAL_LITERAL ;

DECIMAL_LITERAL : '0'
                | NONZERODIGIT DIGIT*
                ;

DIGIT : '0'
      | NONZERODIGIT
      ;

NONZERODIGIT : [1-9] ;

INFINITY : '-'? 'Inf' ;

FLOAT_LITERAL : '-'? FLOAT_REPR ;

FLOAT_REPR : DIGIT+ '.' DIGIT+ EXPONENTPART?
           | '.' DIGIT+ EXPONENTPART?
           | DIGIT EXPONENTPART
           | DIGIT+ EXPONENTPART?
           ;

EXPONENTPART :  ('E' | 'e') ('+' | '-')? DIGIT+ ;

SYMBOLIC_NAME : IDENTIFIER ;

WS : ' ' ;

IDENTIFIER : [a-zA-Z0-9$_]+ ;

string : '\'' STRING_BODY* '\'' ;

STRING_LITERAL : '\'' STRING_BODY* '\'' ;

STRING_BODY : '\u0000' .. '\u0026' // \u0027 is the string delimiter character (')
            | '\u0028' .. '\u01FF'
            | ESCAPED_APOSTROPHE
            ;

ESCAPED_APOSTROPHE : '\\\'' ;
