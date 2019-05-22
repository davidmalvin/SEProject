package com.company;

public class Main {

    public static void main(String[] args) {
        RechercheFilm rechercheFilm = new RechercheFilm("/Users/davidobiangnzue/Desktop/srbdfilm.sqlite");
        try {
            System.out.println(rechercheFilm.Retrouve(""));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        rechercheFilm.FermeBase();

    }
}
