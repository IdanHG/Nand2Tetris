// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/4/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, 
// the screen should be cleared.

//goto LOOP every keyboard change
(LOOP)
    //restart row iterator
    @i
    M=0

    //goto WHITE/BLACK based on keyboard status
    @KBD
    D=M
    @WHITE
    D;JEQ
    @BLACK
    0;JMP

(BLACK)
    //sets row to black 
    @SCREEN
    M=-1

    //row iterator ++
    @i
    M=M+1
    D=M

    //goto next row and color it too
    @SCREEN
    A=D+A
    M=-1

    //goto LOOP if keyboard status changed
    @KBD
    D=M
    @LOOP
    D;JEQ

    //else remain in BLACK
    @BLACK
    0;JMP


(WHITE)
    //sets row to white
    @SCREEN
    M=0
    //row iterator ++
    @i
    M=M+1
    D=M
    
    //goto next row and color it too
    @SCREEN
    A=D+A
    M=0
    
    //goto LOOP if keyboard status changed
    @KBD
    D=M
    @LOOP
    D;JNE
    
    //else remain in WHITE
    @WHITE
    0;JMP



