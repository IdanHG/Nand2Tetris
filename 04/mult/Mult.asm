// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/4/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
// The algorithm is based on repetitive addition.

//set R2=0
@R2
M=0

//set i=R1
@R1
D=M
@i
M=D

(LOOP)
//checks if i==0 => jump to CONT

@i
D=M
@CONT
D;JEQ

//sum R0 to R2
@R0
D=M
@R2
M=D+M

//i = i-1
@i
M=M-1
D=M

//if i>1
@LOOP
D;JGT

//if i == 0 => CONT
@CONT
0;JMP

(CONT)
@CONT
0;JMP

