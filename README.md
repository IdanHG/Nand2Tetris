# Nand2Tetris
NAND2TETRIS Projects Repository Written by Idan Harel Gross & Ido Rosenberg
This repository contains my implementation of projects from the "From NAND to Tetris" course, which builds a modern computer system from first principles. Through these projects, we construct a complete hardware platform and a modern software hierarchy upon which a simple but powerful computer system can be built.
Course Overview
The NAND2TETRIS course is divided into two main parts:

Part I (Hardware, Projects 1-6): Building the hardware platform
Part II (Software, Projects 7-12): Building the software hierarchy

nand2tetris/
├── projects/
│   ├── 01/                  # Boolean Logic
│   ├── 02/                  # Boolean Arithmetic
│   ├── 03/                  # Sequential Logic
│   ├── 04/                  # Machine Language
│   ├── 05/                  # Computer Architecture
│   ├── 06/                  # Assembler
│   ├── 07/                  # VM I: Stack Arithmetic
│   ├── 08/                  # VM II: Program Control
│   ├── 09/                  # High-Level Language
│   ├── 10/                  # Compiler I: Syntax Analysis
│   └── 12/                  # Compiler II: Code Generation


Projects Description
Hardware Projects (1-6)

Boolean Logic: Implementation of basic logic gates using NAND gates
Boolean Arithmetic: ALU (Arithmetic Logic Unit) and adder implementations
Sequential Logic: Registers and memory using D Flip-Flops
Machine Language: Low-level programming in the Hack assembly language
Computer Architecture: CPU and computer implementation
Assembler: Program that translates assembly into binary code

Software Projects (7,9,10,11)

VM I: Implementation of stack arithmetic and memory access commands
VM II: Implementation of branching and function calling commands
High-Level Language: Programming a simple interactive application in Jack
Compiler I: Syntax analyzer for the Jack language
Compiler II: Code generator for the Jack language

Tools and Testing
Each project includes:

HDL files (Hardware Description Language) for hardware projects
Assembly/VM/Jack source files for software projects
Test scripts provided by the course
Additional testing files I've created

Running the Projects

Use the online IDE from NAND2TETRIS official website
1. For projects 1,2,3,5: upload relevant project to the Hardware Simulator and run it
2. For project 4 upload the relevant Aseembly code to the CPU Emulator, translate it to binary code and run it.
3. For project 6 use .asm files to test and run HackAssembler <path to file> in your command line. Load output to the online CPU Emulator and test results. 
4. For projects 7 use .vm files to test and run VMTranslator <path to file> in your command line. Load output to the online CPU Emulator and test results.
5. For project 9 use the NandBlasters directory to the online compiler, compile it and enjoy playing!
6. For projects 10 use .jack files to test and run JackAnalyzer <path to file> in you command line. Compare the .xml output file with the given test files.
7. For projects 11 use .jack files to test and run JackCompiler <path to file> in you command line. Load the output .vm file/s to the VMEmulator and see that the program is behaving as expected.

Resources

Official NAND2TETRIS Website
Digital System Course by Shimon Shocken @ Reichman University
The Elements of Computing Systems (Course Textbook)

License
This project is licensed under the MIT License.

Note: This repository is created for educational purposes as part of our journey through an academic course. The course materials and tools are the property of their respective owners.
