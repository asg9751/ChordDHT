package edu.rit.CSCI652.ChordDHT.impl;

import java.util.Scanner;

public class ChordMenu {
    ChordMenuInterface menuInterface;

    public void setChordMenuInterface(ChordMenuInterface chordMenuInterface) {
        this.menuInterface = chordMenuInterface;
    }

    public interface ChordMenuInterface {
        void invokePrintFingers();

        void invokeInsertKey();

        void invokeLookup();

        void invokeLeave();
    }

    public void printMenu(){

        System.out.println("\n***** Chord Menu *****");

        System.out.println("1. Print Finger Table");
        System.out.println("2. Insert");
        System.out.println("3. Lookup");
        System.out.println("0. Exit\n");
        System.out.print("Choose menu option : ");
    }

    public void showMenu() {
        Scanner in = new Scanner(System.in);

        printMenu();

        boolean exit = false;
        int option = 0;
        try {
            option = in.nextInt();
        } catch (Exception e) {
            showMenu();
            return;
        }
        switch (option) {
            case 1:
                System.out.println("**** Starting print finger table ****\n");
                menuInterface.invokePrintFingers();
                return;
            case 2:
                System.out.println("**** Starting insert key ****\n");
                menuInterface.invokeInsertKey();
                return;

            case 3:
                System.out.println("**** Starting lookup key ****\n");
                menuInterface.invokeLookup();
                return;

            case 0:
                exit = true;
                System.out.println("**** Exiting Chord Menu ****\n");
                menuInterface.invokeLeave();
                return;
            default:
                System.out.println("Invalid choice \n");
        }


        System.out.println("**** Exiting Chord Menu ****\n");
    }
}
