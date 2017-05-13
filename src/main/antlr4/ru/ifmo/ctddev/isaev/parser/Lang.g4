grammar Lang;

// starting point
program
    :    functionDef* codeBlock EOF
    ;

codeBlock
    :    (statement)? (';'statement)*;

statement
    :    'return'? ('skip' | whileLoop | cond | forLoop | repeatLoop | functionCall | assignment |  expr)
    ;

assignment
    : variable ':=' expr
    ;
    
whileLoop
    : 'while' expr 'do' codeBlock 'od'
    ;

forLoop
    : 'for' codeBlock ',' expr ','codeBlock 'do' codeBlock 'od';    
 
repeatLoop
    : 'repeat' codeBlock 'until' expr 
    ;    
   
cond
    : 'if' expr 'then' codeBlock elifs ('else' codeBlock)? 'fi'
    ;

elifs
    : ('elif' expr 'then' codeBlock)*;


argList
    : expr? ( ',' expr)*
    ;
    
functionCall
    : variable '(' argList ')'
    ;

functionDef
    : 'fun' variable '(' argList ')' 'begin' codeBlock 'end'
    ;
    
arrayDeclaration
    : '{'  (expr)? (','expr)* '}'
    ;

pointerAccess    
    : variable ('[' expr ']')+
    ;
  
/* Logical operations have the lowest precedence. */
expr
    :    atom
         |   ('+'|'-') expr
         |   expr ( '*'| '/'| '%') expr
         |   expr ( '+'|'-')  expr
         |   expr ( '<'|'<='|'>'|'>=') expr
         |   expr ( '=='|'!=') expr
         |   expr '&' expr
         |   expr '!!' expr
         |   expr '|' expr
         |   expr '&&' expr
         |   expr '||' expr
         ;
        
atom
    :    boolConst | variable | functionCall | Number | Char | String | arrayDeclaration | pointerAccess | '(' expr ')';
    
variable: 
    Var;
    
boolConst: 
    TRUE | FALSE;    
    
// LEXER
    
// Keywords    
SKIP_ : 'skip';
WHILE : 'while';
TRUE : 'true';
FALSE : 'false';
REPEAT : 'repeat';    
FOR : 'for';    
IF : 'if';  
FUN : 'fun';  
RETURN : 'return';  
DO : 'do';    
THEN : 'then';   
ELSE : 'else';   
UNTIL : 'until';  
BEGIN : 'begin';  
OD : 'od';   
FI : 'fi';    
END : 'end';   
ELIF : 'elif';   

// Accepted characters
fragment Uppercase       :    [A-Z];
fragment Lowercase       :    [a-z];
fragment Letter          :    [a-zA-Z_];
fragment LetterOrDigit   :    [a-zA-Z_0-9];

Number                    :    [0-9]+;
Char                      :    '\'' .+? '\'';
String                    :    '"' .+? '"';
Var                       :    Letter LetterOrDigit*;

// Whitespaces
WS  
    :   [ \t\r\n] -> skip;
    
    