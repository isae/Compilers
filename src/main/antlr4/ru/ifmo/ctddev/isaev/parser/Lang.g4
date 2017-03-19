grammar Lang;

@lexer::members {
    boolean ignore=true;
}

program
    :    functionDef* (statement)? (';'statement)*
    ;
    
statement
    :    (SKIP_RULE | whileLoop | cond | forLoop | repeatLoop | functionCall | assignment |  expr)
    ;

assignment
    : variable ':=' expr
    ;
    
whileLoop
    : WHILE expr DO program OD
    ;

forLoop
    : FOR assignment ',' expr ','assignment DO program OD;    
 
repeatLoop
    : REPEAT program UNTIL expr 
    ;    
   
cond
    : IF expr THEN program ELSE program FI
    ;

argList
    : expr? ( ',' expr)*
    ;
    
functionBody
    :    (RETURN? statement)? (';' RETURN? statement)*
    ;      

functionCall
    : variable '(' argList ')'
    ;

functionDef
    : FUN variable '(' argList ')' BEGIN functionBody END
    ;
  
/* Logical operations have the lowest precedence. */
expr
    :    addition 
         (
             ( '<' addition 
             | '<=' addition
             | '>' addition
             | '>=' addition
             | '==' addition
             | '!=' addition
             | '|' addition
             | '||' addition
             | '&' addition
             | '&&' addition
             )
         )* 
    ;
        
addition
    :    ('+'|'-')? multiplication 
         (
             ( '+' multiplication 
             | '-' multiplication
             )
         )* 
    ;

multiplication
    :    atom ( '*' atom  | '/' atom | '%' atom )* ;

atom
    :    variable
    |    functionCall
    |    Number
    |    '(' expr ')'
    ;
    
variable: 
    Var;
    
SKIP_RULE : 'skip'    ;

WHILE : 'while' { ignore = false; } WS { ignore = true; };    
REPEAT : 'repeat' { ignore = false; } WS { ignore = true; };    
FOR : 'for' { ignore = false; } WS { ignore = true; };    
IF : 'if' { ignore = false; } WS { ignore = true; };  
FUN : 'fun' { ignore = false; } WS { ignore = true; };  
RETURN : 'return' { ignore = false; } WS { ignore = true; };  
  
DO : { ignore = false; } WS+ 'do'  WS+ { ignore = true; };    
THEN : { ignore = false; } WS+ 'then'  WS+ { ignore = true; };   
ELSE : { ignore = false; } WS+ 'else'  WS+ { ignore = true; };   
UNTIL : { ignore = false; } WS+ 'until'  WS+ { ignore = true; };  
BEGIN : { ignore = false; } WS+ 'begin'  WS+ { ignore = true; };  
 
OD : { ignore = false; } WS+ 'od' (WS+|EOF) { ignore = true; };   
FI : { ignore = false; } WS+ 'fi' (WS+|EOF) { ignore = true; };    
END : { ignore = false; } WS+ 'end' (WS+|EOF) { ignore = true; };    

fragment Letter   :    ('A'..'Z'|'a'..'z'|'_');
Number   :    ('0'..'9')+;
Var      :    Letter (Letter | Number)*;
String   :    '"'.*?'"';
Val      :    Number;


WS  
    :   (' ' | '\t' | '\r'| '\n')  { if(ignore) skip(); };
    
    